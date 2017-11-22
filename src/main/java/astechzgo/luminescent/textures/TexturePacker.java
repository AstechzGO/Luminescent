package astechzgo.luminescent.textures;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TexturePacker {
    private Texture atlas;
    
    private Set<AtlasMember> atlasMembers;
    private final Set<Texture> textures = new HashSet<>();
    
    public void addTextures(Texture... textures) {
        addTextures(List.of(textures));
    }
    
    public void addTextures(List<Texture> textures) {
        for(Texture texture : textures) {
            if(texture != null) {
                this.textures.add(texture);
            }
            else {
                this.textures.add(TextureList.findTexture("misc.blank"));
            }
        }
    }
    
    public void pack() {
        List<Texture> byWidth = new ArrayList<>(textures.size());
        List<Texture> byHeight = new ArrayList<>(textures.size());
        
        byWidth.addAll(textures);
        byWidth.sort((o1, o2) -> o2.getAsBufferedImage().getWidth() - o1.getAsBufferedImage().getWidth());
        
        byHeight.addAll(textures);
        byHeight.sort((o1, o2) -> o2.getAsBufferedImage().getHeight() - o1.getAsBufferedImage().getHeight());
        
        Set<AtlasMember> members = new HashSet<>();
        
        int width = Math.max(1024, byWidth.get(0).getAsBufferedImage().getWidth());
        int levelY = 0;
        if(byWidth.get(0).getAsBufferedImage().getWidth() > 1024) {
            members.add(new AtlasMember(byWidth.get(0), 0, 0));
            levelY = byWidth.get(0).getAsBufferedImage().getHeight();
            byHeight.remove(byWidth.get(0));
            byWidth.remove(byWidth.get(0));
        }
        
        while(byHeight.size() != 0) {
            Texture th = byHeight.get(0);
            members.add(new AtlasMember(th, 0, levelY));
            byWidth.remove(th);
            byHeight.remove(th);
            
            // XXX: Texture bleed if width doesn't have an extra space
            int levelX = th.getAsBufferedImage().getWidth() + 1;
            int i = 0;
            while(byWidth.size() != i) {
                Texture tw = byWidth.get(i);
                if(width - levelX >= tw.getAsBufferedImage().getWidth()) {
                    members.add(new AtlasMember(tw, levelX, levelY));
                    byWidth.remove(tw);
                    byHeight.remove(tw);
                    
                    levelX += tw.getAsBufferedImage().getWidth() + 1;
                }
                else {
                    i++;
                }
            }
            
            levelY += th.getAsBufferedImage().getHeight();
        }
        
        atlasMembers = Collections.unmodifiableSet(members);
        buildTexture(atlasMembers, width, levelY);
    }
    
    private void buildTexture(Set<AtlasMember> atlasMembers, int width, int height) {
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        g2.fillRect(0, 0, width, height);
        //draw image
        g2.setColor(oldColor);
        
        for(AtlasMember member : atlasMembers) {
            g2.drawImage(member.texture.getAsBufferedImage(), member.x, member.y, null);
        }
        g2.dispose();
        
        atlas = new Texture("texture-atlas", true, newImage);
    }
    
    public Texture getAtlas() {
        return atlas;
    }
    
    public AtlasMember getAtlasMember(Texture texture) {
        if(atlasMembers != null) {
            for(AtlasMember member : atlasMembers) {
                if(member.texture == texture) {
                    return member;
                }
            }
            return null;
        }
        return null;
    }
    
    public Set<AtlasMember> getAtlasMembers() {
        return new HashSet<>(atlasMembers);
    }
    
    public class AtlasMember  {
        
        private float s = -1, t = -1;
        public final int x, y;
        public final int width, height;
        public final Texture texture;
        
        private AtlasMember(Texture texture, int x, int y) {
            this.texture = texture;
            this.x = x;
            this.y = y;
            this.width = texture.getAsBufferedImage().getWidth();
            this.height = texture.getAsBufferedImage().getHeight();
        }
        
        public void setTexSize(int atlasWidth, int atlasHeight) {
            s = ((float)width) / atlasWidth;
            t = ((float)height) / atlasHeight;
        }
        
        public float getTexWidth() {
            return s;
        }
        
        public float getTexHeight() {
            return t;
        }
    }
}
