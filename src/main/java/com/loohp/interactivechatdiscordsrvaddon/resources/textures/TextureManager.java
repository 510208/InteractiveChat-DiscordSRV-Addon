package com.loohp.interactivechatdiscordsrvaddon.resources.textures;

import com.loohp.interactivechat.libs.org.apache.commons.io.input.BOMInputStream;
import com.loohp.interactivechat.libs.org.json.simple.JSONArray;
import com.loohp.interactivechat.libs.org.json.simple.JSONObject;
import com.loohp.interactivechat.libs.org.json.simple.parser.JSONParser;
import com.loohp.interactivechatdiscordsrvaddon.graphics.ImageGeneration;
import com.loohp.interactivechatdiscordsrvaddon.registry.ResourceRegistry;
import com.loohp.interactivechatdiscordsrvaddon.resources.AbstractManager;
import com.loohp.interactivechatdiscordsrvaddon.resources.ResourceManager;
import com.loohp.interactivechatdiscordsrvaddon.resources.ResourcePackFile;
import com.loohp.interactivechatdiscordsrvaddon.resources.textures.TextureAnimation.TextureAnimationFrames;
import com.loohp.interactivechatdiscordsrvaddon.utils.TintUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureManager extends AbstractManager {

    public static final String SKIN_REQUIRED = "interactivechatdiscordsrvaddon/skin";
    public static final TextureResource MISSING_TEXTURE = new GeneratedTextureResource(ImageGeneration.getMissingImage(16, 16));
    private Map<String, TextureResource> textures;

    public TextureManager(ResourceManager manager) {
        super(manager);
        this.textures = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void loadDirectory(String namespace, ResourcePackFile root) {
        if (!root.exists() || !root.isDirectory()) {
            throw new IllegalArgumentException(root.getAbsolutePath() + " is not a directory.");
        }
        JSONParser parser = new JSONParser();
        Map<String, TextureResource> textures = new HashMap<>();
        Collection<ResourcePackFile> files = root.listFilesRecursively();
        for (ResourcePackFile file : files) {
            try {
                String key = namespace + ":" + file.getRelativePathFrom(root);
                String extension = "";
                if (key.lastIndexOf(".") >= 0) {
                    extension = key.substring(key.lastIndexOf(".") + 1);
                    key = key.substring(0, key.lastIndexOf("."));
                }
                if (extension.equalsIgnoreCase("png")) {
                    textures.put(key, new TextureResource(this, key, file, true));
                } else if (extension.equalsIgnoreCase("mcmeta")) {
                    InputStreamReader reader = new InputStreamReader(new BOMInputStream(file.getInputStream()), StandardCharsets.UTF_8);
                    JSONObject rootJson = (JSONObject) parser.parse(reader);
                    reader.close();
                    TextureAnimation animation = null;
                    if (rootJson.containsKey("animation")) {
                        JSONObject animationJson = (JSONObject) rootJson.get("animation");
                        boolean interpolate = (boolean) animationJson.getOrDefault("interpolate", false);
                        int width = ((Number) animationJson.getOrDefault("width", -1)).intValue();
                        int height = ((Number) animationJson.getOrDefault("height", -1)).intValue();
                        int frametime = ((Number) animationJson.getOrDefault("frametime", -1)).intValue();
                        JSONArray framesArray = ((JSONArray) animationJson.getOrDefault("frames", new JSONArray()));
                        List<TextureAnimationFrames> frames = new ArrayList<>();
                        for (Object obj : framesArray) {
                            if (obj instanceof Number) {
                                frames.add(new TextureAnimationFrames(((Number) obj).intValue(), frametime));
                            } else if (obj instanceof JSONObject) {
                                JSONObject frameJson = (JSONObject) obj;
                                frames.add(new TextureAnimationFrames(((Number) frameJson.get("index")).intValue(), ((Number) frameJson.get("time")).intValue()));
                            }
                        }
                        animation = new TextureAnimation(interpolate, width, height, frametime, frames);
                    }
                    TextureProperties properties = null;
                    if (rootJson.containsKey("texture")) {
                        JSONObject propertiesJson = (JSONObject) rootJson.get("texture");
                        boolean blur = (boolean) propertiesJson.getOrDefault("blur", false);
                        boolean clamp = (boolean) propertiesJson.getOrDefault("clamp", false);
                        int[] mipmaps = ((JSONArray) propertiesJson.getOrDefault("mipmaps", new JSONArray())).stream().mapToInt(each -> ((Number) each).intValue()).toArray();
                        properties = new TextureProperties(blur, clamp, mipmaps);
                    }
                    textures.put(key + "." + extension, new TextureMeta(this, key + "." + extension, file, animation, properties));
                } else {
                    textures.put(key + "." + extension, new TextureResource(this, key, file));
                }
            } catch (Exception e) {
                new RuntimeException("Unable to load block model " + file.getAbsolutePath(), e).printStackTrace();
            }
        }
        this.textures.putAll(textures);
    }

    @Override
    public void reload() {
        int[] grassColorArray;
        TextureResource grassColorMap = getTexture(ResourceRegistry.GRASS_COLORMAP_LOCATION, false);
        if (grassColorMap != null && grassColorMap.isTexture()) {
            grassColorArray = grassColorMap.getTexture(256, 256).getRGB(0, 0, 256, 256, null, 0, 256);
        } else {
            grassColorArray = new int[65536];
            Arrays.fill(grassColorArray, 0xFFFFFF);
        }

        int[] foliageColorArray;
        TextureResource foliageColorMap = getTexture(ResourceRegistry.FOLIAGE_COLORMAP_LOCATION, false);
        if (foliageColorMap != null && foliageColorMap.isTexture()) {
            foliageColorArray = foliageColorMap.getTexture(256, 256).getRGB(0, 0, 256, 256, null, 0, 256);
        } else {
            foliageColorArray = new int[65536];
            Arrays.fill(foliageColorArray, 0xFFFFFF);
        }

        TintUtils.setGrassAndFoliageColorMap(grassColorArray, foliageColorArray);
    }

    public TextureResource getTexture(String resourceLocation) {
        return getTexture(resourceLocation, true);
    }

    public TextureResource getTexture(String resourceLocation, boolean returnMissingTexture) {
        if (!resourceLocation.contains(":")) {
            resourceLocation = ResourceRegistry.DEFAULT_NAMESPACE + ":" + resourceLocation;
        }
        if (resourceLocation.endsWith(".png")) {
            resourceLocation = resourceLocation.substring(0, resourceLocation.length() - 4);
        }
        if (returnMissingTexture) {
            return textures.getOrDefault(resourceLocation, MISSING_TEXTURE);
        } else {
            return textures.get(resourceLocation);
        }
    }

}
