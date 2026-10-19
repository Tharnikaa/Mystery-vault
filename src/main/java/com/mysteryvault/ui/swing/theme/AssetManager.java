package com.mysteryvault.ui.swing.theme;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * High-performance, high-quality asset loader and cache manager.
 * Prioritizes high-resolution asset files, ensures bilinear/bicubic scaling,
 * aspect-ratio cover rendering, and seamless fallbacks.
 */
public class AssetManager {

    private static final Map<String, BufferedImage> imageCache = new HashMap<>();
    private static BufferedImage referenceSheet = null;

    static {
        loadAssets();
    }

    private static void loadAssets() {
        // High-resolution dedicated assets
        loadImageFromFileOrResource("menu_desk.jpg");
        loadImageFromFileOrResource("detective_portrait.jpg");
        loadImageFromFileOrResource("suspect_arjun.jpg");
        loadImageFromFileOrResource("suspect_meera.png");
        loadImageFromFileOrResource("suspect_rohan.png");
        loadImageFromFileOrResource("case_diamond.png");
        loadImageFromFileOrResource("case_library.png");
        loadImageFromFileOrResource("case_poison.png");
        loadImageFromFileOrResource("case_prototype.png");
        loadImageFromFileOrResource("evidence_wineglass.png");

        referenceSheet = loadImageFromFileOrResource("reference_sheet.png");
        if (referenceSheet != null) {
            extractReferenceSubimages();
        }
    }

    private static BufferedImage loadImageFromFileOrResource(String filename) {
        if (filename == null) return null;
        try {
            // Check direct resource path
            InputStream is = AssetManager.class.getResourceAsStream("/assets/" + filename);
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) {
                    imageCache.put(filename, img);
                    return img;
                }
            }

            // Check file system paths
            String[] searchPaths = {
                    "src/main/resources/assets/" + filename,
                    "target/classes/assets/" + filename,
                    "assets/" + filename
            };

            for (String p : searchPaths) {
                File f = new File(p);
                if (f.exists()) {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        imageCache.put(filename, img);
                        return img;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load " + filename + ": " + e.getMessage());
        }
        return null;
    }

    private static void extractReferenceSubimages() {
        if (referenceSheet == null) return;
        try {
            int rw = referenceSheet.getWidth();
            int rh = referenceSheet.getHeight();
            float sx = rw / 1024.0f;
            float sy = rh / 572.0f;

            // Only crop if dedicated high-res asset is not already cached
            cropAndCacheIfMissing("suspect_meera.png", (int)(116 * sx), (int)(230 * sy), (int)(48 * sx), (int)(48 * sy));
            cropAndCacheIfMissing("suspect_rohan.png", (int)(190 * sx), (int)(238 * sy), (int)(48 * sx), (int)(48 * sy));
            cropAndCacheIfMissing("case_diamond.png", (int)(534 * sx), (int)(62 * sy), (int)(72 * sx), (int)(32 * sy));
            cropAndCacheIfMissing("case_library.png", (int)(630 * sx), (int)(62 * sy), (int)(72 * sx), (int)(32 * sy));
            cropAndCacheIfMissing("case_poison.png", (int)(534 * sx), (int)(105 * sy), (int)(72 * sx), (int)(32 * sy));
            cropAndCacheIfMissing("case_prototype.png", (int)(630 * sx), (int)(105 * sy), (int)(72 * sx), (int)(32 * sy));
            cropAndCacheIfMissing("evidence_wineglass.png", (int)(360 * sx), (int)(232 * sy), (int)(122 * sx), (int)(56 * sy));
            cropAndCacheIfMissing("desk_corkboard.png", 0, 0, (int)(240 * sx), (int)(180 * sy));
        } catch (Exception e) {
            System.err.println("Could not extract subimages: " + e.getMessage());
        }
    }

    private static void cropAndCacheIfMissing(String key, int x, int y, int w, int h) {
        if (referenceSheet == null || imageCache.containsKey(key)) return;
        try {
            x = Math.max(0, Math.min(x, referenceSheet.getWidth() - 1));
            y = Math.max(0, Math.min(y, referenceSheet.getHeight() - 1));
            w = Math.max(1, Math.min(w, referenceSheet.getWidth() - x));
            h = Math.max(1, Math.min(h, referenceSheet.getHeight() - y));

            BufferedImage cropped = referenceSheet.getSubimage(x, y, w, h);
            BufferedImage scaled = new BufferedImage(w * 2, h * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaled.createGraphics();
            Theme.applyQualityHints(g2);
            g2.drawImage(cropped, 0, 0, w * 2, h * 2, null);
            g2.dispose();

            imageCache.put(key, scaled);
        } catch (Exception ignored) {}
    }

    public static BufferedImage getImage(String name) {
        if (name == null) return null;
        BufferedImage img = imageCache.get(name);
        if (img != null) return img;

        img = loadImageFromFileOrResource(name);
        if (img != null) return img;

        return generateFallbackImage(name);
    }

    /**
     * Renders a circular image with cover aspect-ratio scaling, crisp antialiasing, and optional border.
     */
    public static BufferedImage getCircularImage(BufferedImage source, int size, Color borderColor, int borderWidth) {
        BufferedImage output = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        Theme.applyQualityHints(g2);

        Ellipse2D.Float circle = new Ellipse2D.Float(borderWidth, borderWidth, size - borderWidth * 2, size - borderWidth * 2);
        g2.setClip(circle);

        if (source != null) {
            int sw = source.getWidth();
            int sh = source.getHeight();
            int targetDim = size - borderWidth * 2;
            double scale = Math.max((double) targetDim / sw, (double) targetDim / sh);
            int dw = (int) Math.round(sw * scale);
            int dh = (int) Math.round(sh * scale);
            int dx = borderWidth + (targetDim - dw) / 2;
            int dy = borderWidth + (targetDim - dh) / 2;

            g2.drawImage(source, dx, dy, dw, dh, null);
        } else {
            g2.setColor(Theme.CARD_BG_SOLID);
            g2.fill(circle);
        }

        g2.setClip(null);
        if (borderColor != null && borderWidth > 0) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.draw(new Ellipse2D.Float(borderWidth / 2f, borderWidth / 2f, size - borderWidth, size - borderWidth));
        }

        g2.dispose();
        return output;
    }

    /**
     * Renders a rounded rectangular image with aspect-ratio cover scaling and border.
     */
    public static BufferedImage getRoundedImage(BufferedImage source, int width, int height, int arc, Color borderColor) {
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = output.createGraphics();
        Theme.applyQualityHints(g2);

        RoundRectangle2D round = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
        g2.setClip(round);

        if (source != null) {
            int sw = source.getWidth();
            int sh = source.getHeight();
            double scale = Math.max((double) width / sw, (double) height / sh);
            int dw = (int) Math.round(sw * scale);
            int dh = (int) Math.round(sh * scale);
            int dx = (width - dw) / 2;
            int dy = (height - dh) / 2;

            g2.drawImage(source, dx, dy, dw, dh, null);
        } else {
            g2.setColor(Theme.CARD_BG);
            g2.fill(round);
        }

        g2.setClip(null);
        if (borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(round);
        }

        g2.dispose();
        return output;
    }

    private static BufferedImage generateFallbackImage(String name) {
        int w = 360, h = 180;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        Theme.applyQualityHints(g2);

        g2.setColor(Theme.CARD_BG);
        g2.fillRect(0, 0, w, h);

        g2.setColor(Theme.BORDER_CARD);
        g2.drawRect(0, 0, w - 1, h - 1);

        g2.setColor(Theme.TEXT_MUTED);
        g2.setFont(Theme.bold(12));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(name, (w - fm.stringWidth(name)) / 2, h / 2);

        g2.dispose();
        imageCache.put(name, img);
        return img;
    }
}
