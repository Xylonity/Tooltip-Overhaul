package dev.xylonity.tooltipoverhaul.client.frame;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DataResult;
import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Conditions are evaluated against the current stack, never cached by item ID
 */
public record FrameConditions(
        Float durabilityMin,
        Float durabilityMax,
        Boolean enchanted,
        Boolean hasCustomName,
        String customName,
        List<ResourceLocation> enchantments,
        List<NbtRule> nbt
) {

    private static final long COMPONENT_WARNING_INTERVAL = 60_000_000_000L;
    private static long lastComponentWarning = System.nanoTime() - COMPONENT_WARNING_INTERVAL;

    public static FrameConditions parse(JsonObject entry) {
        final Float min = percentage(entry, "durabilityMin");
        final Float max = percentage(entry, "durabilityMax");
        if (min != null && max != null && min > max) {
            throw new JsonParseException("durabilityMin exceeds durabilityMax");
        }

        final List<ResourceLocation> enchantments = new ArrayList<>();
        if (entry.has("enchantments")) {
            if (!entry.get("enchantments").isJsonArray()) {
                throw new JsonParseException("enchantments must be an array of IDs");
            }

            for (final JsonElement value : entry.getAsJsonArray("enchantments")) {
                final ResourceLocation id = value.isJsonPrimitive() ? ResourceLocation.tryParse(value.getAsString()) : null;
                if (id == null) {
                    throw new JsonParseException("Invalid enchantment ID: " + value);
                }

                enchantments.add(id);
            }

        }

        final List<NbtRule> nbt = new ArrayList<>();
        if (entry.has("nbt")) {
            if (!entry.get("nbt").isJsonArray()) {
                throw new JsonParseException("nbt must be an array of \"path\", \"path=value\" or \"path!=value\" strings");
            }

            for (final JsonElement value : entry.getAsJsonArray("nbt")) {
                if (!value.isJsonPrimitive()) {
                    throw new JsonParseException("Invalid nbt rule: " + value);
                }

                nbt.add(NbtRule.parse(value.getAsString()));
            }

        }

        return new FrameConditions(min, max, bool(entry, "enchanted"), bool(entry, "hasCustomName"),
                entry.has("customName") ? entry.get("customName").getAsString() : null, List.copyOf(enchantments), List.copyOf(nbt));
    }

    private static Float percentage(JsonObject entry, String key) {
        if (!entry.has(key)) {
            return null;
        }

        float value = entry.get(key).getAsFloat();
        if (!Float.isFinite(value) || value < 0 || value > 100) {
            throw new JsonParseException(key + " must be between 0 and 100");
        }

        return value;
    }

    private static Boolean bool(JsonObject entry, String key) {
        if (!entry.has(key)) {
            return null;
        }

        final String value = entry.get(key).getAsString();
        if (!value.equals("true") && !value.equals("false")) {
            throw new JsonParseException(key + " must be true or false");
        }

        return Boolean.parseBoolean(value);
    }

    public boolean matches(ItemStack stack) {
        if (durabilityMin != null || durabilityMax != null) {
            if (!stack.isDamageableItem()) {
                return false;
            }

            final float remaining = 100f * (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage();
            if (durabilityMin != null && remaining < durabilityMin || durabilityMax != null && remaining > durabilityMax) {
                return false;
            }

        }

        if (hasCustomName != null && stack.has(DataComponents.CUSTOM_NAME) != hasCustomName) {
            return false;
        }

        if (customName != null && (!stack.has(DataComponents.CUSTOM_NAME) || !stack.getHoverName().getString().equals(customName))) {
            return false;
        }

        if (!nbt.isEmpty()) {
            final CompoundTag tag = componentsOf(stack);
            for (final NbtRule rule : nbt) {
                if (!rule.matches(tag)) {
                    return false;
                }

            }

        }

        if (enchanted == null && enchantments.isEmpty()) {
            return true;
        }

        final ItemEnchantments actual = EnchantmentHelper.getEnchantmentsForCrafting(stack);
        if (enchanted != null && !actual.isEmpty() != enchanted) {
            return false;
        }

        for (final ResourceLocation id : enchantments) {
            boolean present = false;
            for (final Holder<Enchantment> holder : actual.keySet()) {
                present |= holder.is(id);
            }

            if (!present) {
                return false;
            }

        }

        return true;
    }

    @Nullable
    private static CompoundTag componentsOf(ItemStack stack) {
        final ClientLevel level = Minecraft.getInstance().level;
        if (level == null || stack.isEmpty()) {
            return null;
        }

        try {
            // ItemStack.CODEC also encodes the count which rejects some modded stacks above 99
            final DataResult<Tag> result = DataComponentPatch.CODEC.encodeStart(
                    level.registryAccess().createSerializationContext(NbtOps.INSTANCE), stack.getComponentsPatch()
            );

            result.error().ifPresent(error -> warnComponentFailure(stack, error.message(), null));

            final Tag encoded = result.result().orElse(null);
            return encoded instanceof CompoundTag compound ? compound : null;
        }
        catch (RuntimeException e) {
            warnComponentFailure(stack, e.getMessage(), e);
            return null;
        }

    }

    private static void warnComponentFailure(ItemStack stack, String message, RuntimeException exception) {
        final long now = System.nanoTime();
        if (now - lastComponentWarning < COMPONENT_WARNING_INTERVAL) {
            return;
        }

        lastComponentWarning = now;

        TooltipOverhaul.LOGGER.warn(
                "Could not read components for {} while checking custom frame NBT conditions: {}.",
                BuiltInRegistries.ITEM.getKey(stack.getItem()), message, exception
        );

    }

    public record NbtRule(
            List<Object> path,
            Operation op,
            String value
    ) {

        public enum Operation {
            EXISTS,
            EQUALS,
            NOT_EQUALS
        }

        public static NbtRule parse(String raw) {
            String text = raw.trim();
            Operation operation = Operation.EXISTS;
            String value = null;

            final int notEquals = text.indexOf("!=");
            final int equals = text.indexOf('=');
            if (notEquals >= 0 && notEquals <= equals) {
                operation = Operation.NOT_EQUALS;
                value = text.substring(notEquals + 2).trim();
                text = text.substring(0, notEquals);
            }
            else if (equals >= 0) {
                operation = Operation.EQUALS;
                value = text.substring(equals + 1).trim();
                text = text.substring(0, equals);
            }

            final List<Object> path = new ArrayList<>();
            for (final String segment : text.trim().split("\\.")) {
                int bracket = segment.indexOf('[');
                final String key = bracket < 0 ? segment : segment.substring(0, bracket);
                if (!key.isEmpty()) {
                    path.add(key);
                }

                while (bracket >= 0) {
                    final int close = segment.indexOf(']', bracket);
                    if (close < 0) {
                        throw new JsonParseException("Invalid nbt rule, unclosed index: " + raw);
                    }

                    try {
                        path.add(Integer.parseInt(segment.substring(bracket + 1, close).trim()));
                    }
                    catch (NumberFormatException exception) {
                        throw new JsonParseException("Invalid nbt rule, index must be a number: " + raw);
                    }

                    bracket = segment.indexOf('[', close);
                }

            }

            if (path.isEmpty()) {
                throw new JsonParseException("Invalid nbt rule, empty path: " + raw);
            }

            return new NbtRule(List.copyOf(path), operation, value);
        }

        public boolean matches(@Nullable CompoundTag root) {
            final Tag found = resolve(root);
            if (found == null) {
                return false;
            }

            return switch (op) {
                case EXISTS -> true;
                case EQUALS -> valueMatches(found);
                case NOT_EQUALS -> !valueMatches(found);
            };

        }

        @Nullable
        private Tag resolve(@Nullable CompoundTag root) {
            Tag current = root;
            for (Object step : path) {
                if (step instanceof String key) {
                    if (!(current instanceof CompoundTag compound)) {
                        return null;
                    }

                    current = compound.get(key);
                }
                else {
                    final int index = (Integer) step;
                    if (!(current instanceof ListTag list) || index < 0 || index >= list.size()) {
                        return null;
                    }

                    current = list.get(index);
                }

                if (current == null) {
                    return null;
                }

            }

            return current;
        }

        private boolean valueMatches(Tag found) {
            if (found instanceof NumericTag numeric) {
                final Double expected = number(value);
                if (expected != null) {
                    return Math.abs(numeric.getAsDouble() - expected) < 1e-6;
                }

            }

            return found.getAsString().equals(value);
        }

        @Nullable
        private static Double number(String text) {
            if (text.equalsIgnoreCase("true")) {
                return 1d;
            }

            if (text.equalsIgnoreCase("false")) {
                return 0d;
            }

            String digits = text;
            if (!digits.isEmpty() && "bBsSlLfFdD".indexOf(digits.charAt(digits.length() - 1)) >= 0) {
                digits = digits.substring(0, digits.length() - 1);
            }

            try {
                return Double.parseDouble(digits);
            }
            catch (NumberFormatException exception) {
                return null;
            }

        }

    }

}