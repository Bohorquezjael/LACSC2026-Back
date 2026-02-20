package com.innovawebJT.lacsc.service.imp;

import com.innovawebJT.lacsc.enums.FileCategory;
import com.innovawebJT.lacsc.service.IFileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService implements IFileStorageService {

    private final Path root = Paths.get("storage");

    @Override
    public String store(
        Long userId,
        FileCategory category,
        String title,
        MultipartFile file
    ) {

        validatePdf(file);

        try {
            Path dir = buildPath(userId, category);
            Files.createDirectories(dir);

            String filename = buildFilename(category, title);
            Path target = dir.resolve(filename);

            Files.copy(
                file.getInputStream(),
                target,
                StandardCopyOption.REPLACE_EXISTING
            );

            return root.relativize(target).toString();

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar archivo", e);
        }
    }

    @Override
    public String replace(String oldPath, MultipartFile newFile) {
        delete(oldPath);

        Path old = root.resolve(oldPath);
        return store(
            extractUserId(old),
            extractCategory(old),
            extractTitle(old),
            newFile
        );
    }

    @Override
    public Resource load(String path) {
        try {
            Path file = root.resolve(path);
            Resource resource = new UrlResource(file.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Archivo no accesible");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer archivo", e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Files.deleteIfExists(root.resolve(path));
        } catch (IOException e) {
            throw new RuntimeException("Error al borrar archivo", e);
        }
    }

    /* ---------------- helpers ---------------- */

    private Path buildPath(Long userId, FileCategory category) {
        return root.resolve(
            "users/user_" + userId + "/" + category.name().toLowerCase()
        );
    }

    private String buildFilename(FileCategory category, String title) {
        return category.name().toLowerCase() + "_" + title + ".pdf";
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Solo se permiten PDFs");
        }
    }

    private Long extractUserId(Path path) {
        for (Path part : path) {
            String name = part.toString();
            if (name.startsWith("user_")) {
                return Long.valueOf(name.replace("user_", ""));
            }
        }
        throw new IllegalArgumentException("No se pudo extraer userId del path: " + path);
    }

    private FileCategory extractCategory(Path path) {
        for (Path part : path) {
            String name = part.toString().toUpperCase();
            try {
                return FileCategory.valueOf(name);
            } catch (IllegalArgumentException ignore) {
                log.error(ignore.getMessage());
            }
        }
        throw new IllegalArgumentException("No se pudo extraer categoría del path: " + path);
    }

    private String extractTitle(Path path) {
        String name = path.getFileName().toString();
        String[] parts = name.split("_", 3);
        if (parts.length >= 3) {
            return parts[2].replace(".pdf", "");
        }
        return name.replace(".pdf", "");
    }
}
