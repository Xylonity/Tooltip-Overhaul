package dev.xylonity.tooltipoverhaul.client.frame;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * One effective resourcepack document and its optional copy (replacement)
 */
public record CustomFrameSource(
        ResourceLocation location,
        Path file,
        @Nullable Resource resource,
        boolean primary
) {

    public boolean customized() {
        return Files.exists(file);
    }

    public String packName() {
        return resource == null ? "" : resource.sourcePackId();
    }

    public void prepareSave() throws IOException {
        CustomFrameStorage.prepareSave(this);
    }

    public Reader openReader() throws IOException {
        if (customized()) {
            return Files.size(file) == 0 ? new StringReader("{\"frames\":[]}") : Files.newBufferedReader(file, StandardCharsets.UTF_8);
        }

        if (resource != null) {
            return resource.openAsReader();
        }

        if (primary) {
            return new StringReader("{\"frames\":[]}");
        }

        throw new FileNotFoundException(location.toString());
    }

    void initializePrimary() throws IOException {
        if (!primary || customized()) {
            return;
        }

        Files.createDirectories(file.getParent());
        if (resource == null) {
            Files.writeString(file, "{\n  \"frames\": []\n}\n", StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        }
        else {
            try (final InputStream input = resource.open()) {
                Files.copy(input, file);
            }

        }

    }

    public Path restoreOriginal() throws IOException {
        if (resource == null || !customized()) {
            throw new IOException("No customized resource to restore");
        }

        try (final Reader reader = resource.openAsReader()) {
            CustomFrameStorage.read(reader);
        }

        final Path backup = Files.createTempFile(file.getParent(), file.getFileName() + ".restored-", ".bak");
        try {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (AtomicMoveNotSupportedException ignored)  {
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.deleteIfExists(CustomFrameStorage.defaultsFile(this));

        return backup;
    }

}