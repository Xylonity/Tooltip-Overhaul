package dev.xylonity.tooltipoverhaul.client.screen.config;

import dev.xylonity.tooltipoverhaul.TooltipOverhaul;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameLoader;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameManager;
import dev.xylonity.tooltipoverhaul.client.frame.CustomFrameSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static dev.xylonity.tooltipoverhaul.client.screen.config.ConfigScreenStyle.*;

public final class FrameSourceScreen extends AbstractConfigScreen {

    private List<Entry> sources = List.of();
    private List<Entry> visibleSources = List.of();

    private ResourceLocation selected = CustomFrameLoader.DEFAULT_SOURCE;
    private ConfigNavigationList<Entry> list;

    private SearchBox search;

    private FlatButton edit;
    private FlatButton restore;

    private String query = "";
    private String status = "";

    private boolean failed;

    private int panelX;
    private int panelWidth;
    private int listBottom;

    public FrameSourceScreen(Screen parent, int accent) {
        super(Component.literal("Tooltip Overhaul"), parent, accent);
    }

    public static Screen editorOrSelector(Screen parent, int accent) {
        final List<CustomFrameSource> sources = CustomFrameLoader.discoverSources(Minecraft.getInstance().getResourceManager(), TooltipOverhaul.PLATFORM.getConfigPath());
        return sources.size() == 1 && sources.get(0).primary() ? new FrameEditorScreen(parent, accent, sources.get(0)) : new FrameSourceScreen(parent, accent);
    }

    @Override
    protected void initScreen() {
        panelWidth = Math.max(80, Math.min(680, width - 24));
        panelX = (width - panelWidth) / 2;
        listBottom = Math.max(HEADER_HEIGHT + 74, height - 94);
        sources = CustomFrameLoader.discoverSources(minecraft.getResourceManager(), TooltipOverhaul.PLATFORM.getConfigPath())
                .stream().map(source -> new Entry(source,
                        source.primary() ? "Tooltip Overhaul" : TooltipOverhaul.PLATFORM
                                .getModDisplayName(source.location().getNamespace()).orElse(source.location().getNamespace()),
                        source.customized())).toList();

        search = addRenderableWidget(new SearchBox(font, panelX, HEADER_HEIGHT + 6, panelWidth, 18, accent));
        list = addRenderableWidget(new ConfigNavigationList<>(panelX, HEADER_HEIGHT + 30, panelWidth,
                listBottom - HEADER_HEIGHT - 30, Component.translatable(key("files")), accent, List.of(),
                new ConfigNavigationList.Adapter<>() {

                    @Override
                    public Component label(Entry entry, int index) {
                        return Component.literal(entry.name() + " / " + relativePath(entry.source()));
                    }

                    @Override
                    public Component description(Entry entry, int index) {
                        return Component.literal(entry.source().location().getNamespace() + "  ·  " + state(entry));
                    }

                    @Override
                    public int rowHeight(Entry entry, int index, int contentWidth) {
                        return 34;
                    }

                }, index -> {
                    if (index >= 0 && index < visibleSources.size()) {
                        selected = visibleSources.get(index).source().location();
                        updateButtons();
                    }

                }));

        list.setReorderingEnabled(false);

        final int buttonWidth = Math.min(140, (panelWidth - 16) / 3);
        final int start = (width - buttonWidth * 3 - 16) / 2;
        edit = addRenderableWidget(new FlatButton(start, height - 26, buttonWidth, 18, Component.translatable(key("edit")), ignored -> openSelected(), accent, true));
        restore = addRenderableWidget(new FlatButton(start + buttonWidth + 8, height - 26, buttonWidth, 18, Component.translatable(key("restore")), ignored -> confirmRestore(), accent));
        addRenderableWidget(new FlatButton(start + (buttonWidth + 8) * 2, height - 26, buttonWidth, 18, Component.translatable("tooltipoverhaul.config.frames.back"), ignored -> onClose(), accent));
        search.setResponder(value -> { query = value; filter(); });
        search.setValue(query);

        filter();
    }

    private void filter() {
        final String needle = query.toLowerCase(Locale.ROOT).trim();
        visibleSources = sources.stream().filter(entry -> (entry.name() + " " + entry.source().location() + " " + entry.source().packName()).toLowerCase(Locale.ROOT).contains(needle)).toList();
        list.setItems(visibleSources);
        int index = -1;
        for (int i = 0; i < visibleSources.size(); i++) {
            if (visibleSources.get(i).source().location().equals(selected)) {
                index = i;
            }

        }

        if (index < 0 && !visibleSources.isEmpty()) {
            index = 0;
            selected = visibleSources.get(0).source().location();
        }

        list.setSelectedIndex(index);
        list.resetScroll();

        updateButtons();
    }

    private Entry selection() {
        return visibleSources.stream().filter(entry -> entry.source().location().equals(selected)).findFirst().orElse(null);
    }

    private void updateButtons() {
        Entry entry = selection();
        edit.active = entry != null;
        restore.active = entry != null && !entry.source().primary() && entry.customized();
    }

    private void openSelected() {
        Entry entry = selection();
        if (entry != null) {
            minecraft.setScreen(new FrameEditorScreen(this, accent, entry.source()));
        }

    }

    private void confirmRestore() {
        Entry entry = selection();
        if (entry == null || !restore.active) {
            return;
        }

        minecraft.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                try {
                    final Path backup = entry.source().restoreOriginal();
                    CustomFrameManager.reset();
                    CustomFrameManager.initialize();
                    status = I18n.get(key("restored"), backup.getFileName());
                    failed = false;
                }
                catch (Exception exception)  {
                    TooltipOverhaul.LOGGER.error("Could not restore {}", entry.source().location(), exception);
                    status = I18n.get(key("restore_failed"), exception.getMessage());
                    failed = true;
                }

            }

            minecraft.setScreen(this);
        }, Component.translatable(key("restore")), Component.translatable(key("restore_confirm"), entry.source().location().toString())));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        restoreGuiRenderState(graphics);
        renderScreenShell(graphics, I18n.get(key("subtitle")));

        drawCard(graphics, panelX, listBottom + 6, panelX + panelWidth, height - 36, 0xFF151517, 0xFF29292E);

        final Entry entry = selection();
        final int y = listBottom + 16;
        if (entry != null) {
            String origin = entry.source().primary() ? I18n.get(key("user_file")) : I18n.get(key("origin"), entry.source().packName());
            graphics.drawString(font, font.plainSubstrByWidth(origin, panelWidth - 16), panelX + 8, y, 0xFFCCCCCC, false);

            String path = TooltipOverhaul.PLATFORM.getConfigPath().toAbsolutePath().normalize().relativize(entry.source().file().toAbsolutePath().normalize()).toString();
            graphics.drawString(font, font.plainSubstrByWidth(path, panelWidth - 16), panelX + 8, y + 12, 0xFF92929E, false);

            String hint = status.isEmpty() ? I18n.get(key(entry.source().primary() ? "primary_hint" : entry.customized() ? "custom_hint" : "original_hint")) : status;
            graphics.drawString(font, font.plainSubstrByWidth(hint, panelWidth - 16), panelX + 8, y + 24, failed ? 0xFFFF7777 : 0xFF92929E, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
        if (visibleSources.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("tooltipoverhaul.config.empty_search", query), width / 2, HEADER_HEIGHT + 62, 0xFF999999);
        }

        if (entry != null && mouseX >= panelX && mouseX < panelX + panelWidth && mouseY >= listBottom + 6 && mouseY < height - 36) {
            graphics.renderTooltip(font, font.split(Component.literal(entry.source().location() + "\n" + entry.source().file() + (status.isEmpty() ? "" : "\n" + status)), Math.min(420, width - 24)), mouseX, mouseY);
        }

    }

    @Override
    protected void tickScreen() {
        ;;
    }

    @Override
    protected boolean handleKeyPressed(int key, int scanCode, int modifiers) {
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && (search.isFocused() || list.isFocused())) {
            openSelected();
            return true;
        }

        return super.handleKeyPressed(key, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        returnToParent();
    }

    private static String relativePath(CustomFrameSource source) {
        return source.location().getPath().substring("tooltipoverhaul/".length());
    }

    private static String state(Entry entry) {
        return I18n.get(key(entry.source().primary() ? "user_file" : entry.customized() ? "customized" : "original"));
    }

    private static String key(String name) {
        return "tooltipoverhaul.config.sources." + name;
    }

    private record Entry(
            CustomFrameSource source,
            String name,
            boolean customized
    ) {
        ;;
    }

}
