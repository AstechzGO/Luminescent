package astechzgo.luminescent.rendering;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVulkan;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.shader.ShaderList;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.textures.TexturePacker;
import astechzgo.luminescent.utils.DisplayUtils;

public class Vulkan {
    
    private static final Vulkan vulkanInstance = new Vulkan();
    
    private static float red = 0.0f;
    private static float green = 0.4f;
    private static float blue = 0.6f;
    private static float alpha = 1.0f;

    private static boolean lighting = true;
    
    public static void init() {
        vulkanInstance.initVulkan();
    }
    
    public static void recreate() {
        if(DisplayUtils.getDisplayWidth() == 0 || DisplayUtils.getDisplayHeight() == 0) return;
        vulkanInstance.recreateSwapChain();
    }
    
    public static void tick() {
        vulkanInstance.updateUniformBuffer();
        vulkanInstance.drawFrame();
    }
    
    public static void shutdown() {
        vulkanInstance.cleanup();
    }
    
    public static void setClearColour(float redVal, float greenVal, float blueVal, float alphaVal) {
        red = redVal;
        green = greenVal;
        blue = blueVal;
        alpha = alphaVal;
    }

    public static Color getClearColour() {
        return new Color(red, green, blue, alpha);
    }

    public static void setDoLighting(boolean lighting) {
        Vulkan.lighting = lighting;
    }

    public static boolean getDoLighting() {
        return lighting;
    }
    
    public static long getShaderHandle(byte[] shaderCode) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            return vulkanInstance.createShaderModule(stack.bytes(shaderCode));
        }
    }
    
    public static void createWindowSurface() {
        vulkanInstance.createSurface();
    }

    public final class RawImage {
        private final int width, height;
        private final int colourFormat;
        private final ByteBuffer data;

        private RawImage(int width, int height, int colourFormat, ByteBuffer data) {
            this.width = width;
            this.height = height;
            this.colourFormat = colourFormat;
            this.data = data;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int getColourFormat() {
            return colourFormat;
        }

        public ByteBuffer getData() {
            return data;
        }

        public void free() {
            MemoryUtil.memFree(data);
        }
    }

    public static RawImage readPixels() {
        return vulkanInstance.readPixelsToArray();
    }

    private final String[] validationLayers = { 
        "VK_LAYER_LUNARG_standard_validation",
    };
    
    private final String[] deviceExtensions = {
        KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME
    };

    private final VkDebugReportCallbackEXT debugCallback = VkDebugReportCallbackEXT.create(
        (flags, objectType, object, location, messageCode, pLayerPrefix, pMessage, pUserData) -> {
            System.err.println("validation layer: " + VkDebugReportCallbackEXT.getString(pMessage));
            return VK10.VK_FALSE;
    });
    
    private long debugCallbackAddress;
    
    private long surface;
    
    private VkInstance instance;
    
    private VkPhysicalDevice physicalDevice;
    
    private VkDevice device;

    private VkQueue graphicsQueue;
    
    private VkQueue presentQueue;

    private long swapChain;
    
    private long[] swapChainImages;
    
    private int swapChainImageFormat;

    private VkExtent2D swapChainExtent;
    
    private long[] swapChainImageViews;
    
    private long vertShaderModule;
    private long fragShaderModule;
  
    private long renderPass;
    
    private long descriptorSetLayout;
    private long pipelineLayout;
    
    private long graphicsPipeline;
    
    private long[] swapChainFramebuffers;
    
    private long commandPool;
    
    private VkCommandBuffer[] commandBuffers;
    
    private long imageAvailableFence;
    private long renderFinishedSemaphore;
    
    private final List<List<Vertex>> vertices = new ArrayList<>();
    private final List<List<Integer>> indices = new ArrayList<>();
    private final List<Texture> textures = new ArrayList<>();
    private final List<Integer> frameCount = new ArrayList<>();
    private final List<List<Supplier<Matrix4f>>> matrices = new ArrayList<>();
    private final List<Supplier<Integer>> currentFrames = new ArrayList<>();
    private final List<Supplier<Boolean>> doLighting = new ArrayList<>();
    
    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;
    private long uniformViewBuffer;
    private long uniformViewBufferMemory;
    private long uniformModelBuffer;
    private long uniformModelBufferMemory;
    private long uniformLightsBuffer;
    private long uniformLightsBufferMemory;
    
    private long dynamicAlignment;
    private long lightAlignment;
    
    private long descriptorPool;
    private long descriptorSet;
    
    private TexturePacker texturePacker;
    private long textureImage;
    private long textureImageMemory;
    
    private long textureImageView;
    private long textureSampler;

    private int imageIndex;

    private void initVulkan() {
        createInstance();
        setUpDebugCallback();
        createSurface();
        pickPhysicalDevice();
        createLogicalDevice();
        createSwapChain();
        createImageViews();
        createRenderPass();
        createDescriptorSetLayout();
        ShaderList.initShaderList();
        createGraphicsPipeline();
        createFramebuffers();
        createCommandPool();
        createTextureSampler();
        createSynchronizationPrimitives();
    }
    
    private void createTextureSampler() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO)
                .magFilter(VK10.VK_FILTER_NEAREST)
                .minFilter(VK10.VK_FILTER_NEAREST)
                .addressModeU(VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
                .addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .anisotropyEnable(true)
                .maxAnisotropy(16)
                .borderColor(VK10.VK_BORDER_COLOR_INT_OPAQUE_BLACK)
                .unnormalizedCoordinates(false)
                .compareEnable(false)
                .compareOp(VK10.VK_COMPARE_OP_ALWAYS)
                .mipmapMode(VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
                .mipLodBias(0.0f)
                .minLod(0.0f)
                .maxLod(0.0f)
                .pNext(VK10.VK_NULL_HANDLE)
                .flags(0);
            
             long[] textureSamplerAddress = new long[] { 0 };
             if(VK10.vkCreateSampler(device, samplerInfo, null, textureSamplerAddress) != VK10.VK_SUCCESS) {
                 throw new RuntimeException("failed to create texture sampler!");
             }
             textureSampler = textureSamplerAddress[0];
        }
    }
    
    private void createTextureImageView() {
        textureImageView = createImageView(textureImage, VK10.VK_FORMAT_R8G8B8A8_UNORM);
    }
    
    private long createImageView(long image, int format) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO)
                .image(image)
                .viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
                .format(format)
                .subresourceRange(VkImageSubresourceRange.callocStack(stack)
                    .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1));
            
            long[] textureImageViewAddress = new long[] { 0 };
            if(VK10.vkCreateImageView(device, viewInfo, null, textureImageViewAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create texture image view!");
            }
            return textureImageViewAddress[0];
        }
    }
    
    private void createTextureImage() {
        texturePacker = new TexturePacker();
        texturePacker.addTextures(textures);
        texturePacker.pack();
        
        for(TexturePacker.AtlasMember member : texturePacker.getAtlasMembers()) {
            member.setTexSize(texturePacker.getAtlas().getAsBufferedImage().getWidth(), texturePacker.getAtlas().getAsBufferedImage().getHeight());
        }
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
//            int[] texWidth = new int[] { 0 };
//            int[] texHeight = new int[] { 0 };
//            int[] texChannels = new int[] { 0 };
//            
//            ByteBuffer file = stack.bytes(SystemUtils.readFile("textures/player/texture.png"));
//            
//            ByteBuffer pixels
//                = STBImage.stbi_load_from_memory(file, texWidth, texHeight, texChannels, STBImage.STBI_rgb_alpha);
//            int imageSize = texWidth[0] * texHeight[0] * STBImage.STBI_rgb_alpha;
//            
//            if(pixels == null) {
//                throw new RuntimeException("failed to load texture image!");
//            }
//            
//            
            Texture texture = texturePacker.getAtlas();
            
            ByteBuffer pixels = texture.getAsByteBuffer();
            int width = texture.getAsBufferedImage().getWidth();
            int height = texture.getAsBufferedImage().getHeight();
            int imageSize = width * height * 4;
            
            long[] stagingBufferAddress = { 0 };
            long[] stagingBufferMemoryAddress = { 0 };
            
            createBuffer(imageSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferAddress, stagingBufferMemoryAddress);
            
            PointerBuffer data = stack.mallocPointer(1);
            VK10.vkMapMemory(device, stagingBufferMemoryAddress[0], 0, imageSize, 0, data);
                ByteBuffer buffer = data.getByteBuffer(imageSize).put(pixels);
                buffer.flip();
            VK10.vkUnmapMemory(device, stagingBufferMemoryAddress[0]);
            
//            pixels.flip();
//            STBImage.stbi_image_free(pixels);
            
            long[] textureImageAddress = new long[] { 0 };
            long[] textureImageMemoryAddress = new long[] { 0 };
            createImage(width, height, VK10.VK_FORMAT_R8G8B8A8_UNORM, VK10.VK_IMAGE_TILING_OPTIMAL,
                            VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                            textureImageAddress, textureImageMemoryAddress);
            textureImage = textureImageAddress[0];
            textureImageMemory = textureImageMemoryAddress[0];

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();
            transitionImageLayout(commandBuffer, textureImage, VK10.VK_ACCESS_HOST_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_IMAGE_LAYOUT_PREINITIALIZED,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT);
            endSingleTimeCommands(commandBuffer);
            copyBufferToImage(stagingBufferAddress[0], textureImage, width, height);
            commandBuffer = beginSingleTimeCommands();
            transitionImageLayout(commandBuffer, textureImage, VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_SHADER_READ_BIT, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT);
            endSingleTimeCommands(commandBuffer);
            
            VK10.vkDestroyBuffer(device, stagingBufferAddress[0], null);
            VK10.vkFreeMemory(device, stagingBufferMemoryAddress[0], null);
        }
    }
    
    private void transitionImageLayout(VkCommandBuffer cmdBuffer, long image, int srcAccessMask, int dstAccessMask, int oldLayout, int newLayout, int srcStageMask, int dstStageMask) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier barrier = VkImageMemoryBarrier.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER)
                .oldLayout(oldLayout)
                .newLayout(newLayout)
                .srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
                .image(image)
                .subresourceRange(VkImageSubresourceRange.callocStack(stack)
                    .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1))
                .srcAccessMask(srcAccessMask)
                .dstAccessMask(dstAccessMask);

            VK10.vkCmdPipelineBarrier(
                    cmdBuffer,
                VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                0,
                null,
                null,
                VkImageMemoryBarrier.mallocStack(1, stack).put(barrier).flip()
            );
        }
    }
    
    private void copyBufferToImage(long buffer, long image, int width, int height) {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferImageCopy region = VkBufferImageCopy.callocStack(stack)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                
                .imageSubresource(VkImageSubresourceLayers.callocStack(stack)
                   .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                   .mipLevel(0)
                   .baseArrayLayer(0)
                   .layerCount(1))
                
                .imageOffset(VkOffset3D.callocStack(stack).set(0, 0, 0))
                .imageExtent(VkExtent3D.callocStack(stack).set(
                    width,
                    height,
                    1
                ));
            
            VK10.vkCmdCopyBufferToImage(
                commandBuffer,
                buffer,
                image,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VkBufferImageCopy.callocStack(1, stack).put(region).flip()
            );
        }
        
        endSingleTimeCommands(commandBuffer);
    }
    
    private void copyImageToBuffer(long buffer, long image, int width, int height) {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferImageCopy region = VkBufferImageCopy.callocStack(stack)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                
                .imageSubresource(VkImageSubresourceLayers.callocStack(stack)
                   .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                   .mipLevel(0)
                   .baseArrayLayer(0)
                   .layerCount(1))
                
                .imageOffset(VkOffset3D.callocStack(stack).set(0, 0, 0))
                .imageExtent(VkExtent3D.callocStack(stack).set(
                    width,
                    height,
                    1
                ));
            
            VK10.vkCmdCopyImageToBuffer(
                commandBuffer,
                image,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                buffer,
                VkBufferImageCopy.callocStack(1, stack).put(region).flip()
            );

        }
        
        endSingleTimeCommands(commandBuffer);
    }
    
    private void createImage(int width, int height, int format, int tiling, int usage, int properties, long[] imageAddress, long[] imageMemoryAddress) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                .imageType(VK10.VK_IMAGE_TYPE_2D)
                .extent(VkExtent3D.callocStack(stack)
                    .width(width)
                    .height(height)
                    .depth(1))
                .mipLevels(1)
                .arrayLayers(1)
                .format(format)
                .tiling(tiling)
                .initialLayout(VK10.VK_IMAGE_LAYOUT_PREINITIALIZED)
                .usage(usage)
                .samples(VK10.VK_SAMPLE_COUNT_1_BIT)
                .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
                .flags(0);
            
            if(VK10.vkCreateImage(device, imageInfo, null, imageAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create image!");
            }
            
        
            VkMemoryRequirements memoryRequirements = VkMemoryRequirements.mallocStack(stack);
            VK10.vkGetImageMemoryRequirements(device, imageAddress[0], memoryRequirements);
            
            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .allocationSize(memoryRequirements.size())
                .memoryTypeIndex(findMemoryType(memoryRequirements.memoryTypeBits(), properties));
            
            if(VK10.vkAllocateMemory(device, allocInfo, null, imageMemoryAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to allocate image memory!");
            }
            
            VK10.vkBindImageMemory(device, imageAddress[0], imageMemoryAddress[0], 0);
        }
    }
    
    private void createDescriptorSet() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer layouts = stack.callocLong(1).put(descriptorSetLayout);
            layouts.flip();
            
            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO)
                .descriptorPool(descriptorPool)
                .pSetLayouts(layouts);
            
            long[] descriptorSetAddress = new long[] { 0 };
            if(VK10.vkAllocateDescriptorSets(device, allocInfo, descriptorSetAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to allocate descriptor set!");
            }
            descriptorSet = descriptorSetAddress[0];
                
            VkDescriptorBufferInfo viewBufferInfo = VkDescriptorBufferInfo.callocStack(stack)
                .buffer(uniformViewBuffer)
                .offset(0)
                .range(2 * 4 * 4 * Float.BYTES);
            
            VkDescriptorBufferInfo modelBufferInfo = VkDescriptorBufferInfo.callocStack(stack)
                .buffer(uniformModelBuffer)
                .offset(0)
                .range(dynamicAlignment);
            
            VkDescriptorImageInfo imageInfo = VkDescriptorImageInfo.callocStack(stack)
                .imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                .imageView(textureImageView)
                .sampler(textureSampler);

            VkDescriptorBufferInfo lightsBufferInfo = VkDescriptorBufferInfo.callocStack(stack)
                .buffer(uniformLightsBuffer)
                .offset(0)
                .range(LightSource.LIGHTS * lightAlignment);

            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(4, stack);
            
            descriptorWrites.get(0)
                .sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(0)
                .dstArrayElement(0)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .pBufferInfo(VkDescriptorBufferInfo.mallocStack(1, stack).put(viewBufferInfo).flip())
                .pImageInfo(null)
                .pTexelBufferView(null)
                .pNext(VK10.VK_NULL_HANDLE);
            
            descriptorWrites.get(1)
                .sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(1)
                .dstArrayElement(0)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                .pBufferInfo(VkDescriptorBufferInfo.mallocStack(1, stack).put(modelBufferInfo).flip())
                .pImageInfo(null)
                .pTexelBufferView(null)
                .pNext(VK10.VK_NULL_HANDLE);
            
            descriptorWrites.get(2)
                .sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(2)
                .dstArrayElement(0)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .pBufferInfo(null)
                .pImageInfo(VkDescriptorImageInfo.mallocStack(1, stack).put(imageInfo).flip())
                .pTexelBufferView(null)
                .pNext(VK10.VK_NULL_HANDLE);

            descriptorWrites.get(3)
                .sType(VK10.VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET)
                .dstSet(descriptorSet)
                .dstBinding(3)
                .dstArrayElement(0)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .pBufferInfo(VkDescriptorBufferInfo.mallocStack(1, stack).put(lightsBufferInfo).flip())
                .pImageInfo(null)
                .pTexelBufferView(null)
                .pNext(VK10.VK_NULL_HANDLE);
            
            VK10.vkUpdateDescriptorSets(device, descriptorWrites, null);
        }
    }
    
    private void createDescriptorPool() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.mallocStack(4, stack);
            
            poolSizes.get(0)
                .type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1);
            poolSizes.get(1)
                .type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                .descriptorCount(1);
            poolSizes.get(2)
                .type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(1);
            poolSizes.get(3)
                .type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(1);
            
            VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO)
                .pPoolSizes(poolSizes)
                .maxSets(1);
            
            long[] descriptorPoolAddress = new long[] { 0 };
            if(VK10.vkCreateDescriptorPool(device, poolInfo, null, descriptorPoolAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create descriptor pool!");
            }
            descriptorPool = descriptorPoolAddress[0];
        }
    }
    
    private void createUniformBuffer() {
        int viewBufferSize = 2 * 4 * 4 * Float.BYTES;
        
        long[] uniformViewBufferAddress = new long[] { 0 };
        long[] uniformViewBufferMemoryAddress = new long[] { 0 };
        
        createBuffer(viewBufferSize, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, uniformViewBufferAddress, uniformViewBufferMemoryAddress);
        
        uniformViewBuffer = uniformViewBufferAddress[0];
        uniformViewBufferMemory = uniformViewBufferMemoryAddress[0];
        
        long uboAlignment = 0;
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
            VK10.vkGetPhysicalDeviceProperties(physicalDevice, deviceProperties);
            
            uboAlignment = deviceProperties.limits().minUniformBufferOffsetAlignment();
        }
        
        int instanceUBOSize = (4 * 4 * Float.BYTES) + (2 * Integer.BYTES) + (Float.BYTES) + 4;
        dynamicAlignment = (instanceUBOSize / uboAlignment) * uboAlignment + ((instanceUBOSize % uboAlignment) > 0 ? uboAlignment : 0);
        long modelBufferSize = flatSize(matrices) * dynamicAlignment;

        long[] uniformModelBufferAddress = new long[] { 0 };
        long[] uniformModelBufferMemoryAddress = new long[] { 0 };
        
        createBuffer(modelBufferSize, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, uniformModelBufferAddress, uniformModelBufferMemoryAddress);
        
        uniformModelBuffer = uniformModelBufferAddress[0];
        uniformModelBufferMemory = uniformModelBufferMemoryAddress[0];

        long[] uniformLightsBufferAddress = new long[] { 0 };
        long[] uniformLightsBufferMemoryAddress = new long[] { 0 };

        int lightUBOSize = 3 * Float.BYTES;
        int lightUniformAlignment = 16;
        lightAlignment = (lightUBOSize / lightUniformAlignment) * lightUniformAlignment + ((lightUBOSize % lightUniformAlignment) > 0 ? lightUniformAlignment : 0);
        long lightsBufferSize = LightSource.LIGHTS * dynamicAlignment;

        createBuffer(lightsBufferSize, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, uniformLightsBufferAddress, uniformLightsBufferMemoryAddress);

        uniformLightsBuffer = uniformLightsBufferAddress[0];
        uniformLightsBufferMemory = uniformLightsBufferMemoryAddress[0];
    }
    
    private void createDescriptorSetLayout() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetLayoutBinding viewUBOLayoutBinding = VkDescriptorSetLayoutBinding.mallocStack(stack)
                .binding(0)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT);
            
            VkDescriptorSetLayoutBinding modelUBOLayoutBinding = VkDescriptorSetLayoutBinding.mallocStack(stack)
                .binding(1)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT);
            
            VkDescriptorSetLayoutBinding samplerLayoutBinding = VkDescriptorSetLayoutBinding.mallocStack(stack)
                .binding(2)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding lightsUBOLayoutBinding = VkDescriptorSetLayoutBinding.mallocStack(stack)
                .binding(3)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.mallocStack(4, stack)
                .put(viewUBOLayoutBinding)
                .put(modelUBOLayoutBinding)
                .put(samplerLayoutBinding)
                .put(lightsUBOLayoutBinding).flip();
            
            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO)
                .pBindings(bindings);
            
            long[] descriptorSetLayoutAddress = new long[] { 0 };
            if(VK10.vkCreateDescriptorSetLayout(device, layoutInfo, null, descriptorSetLayoutAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create descriptor set layout!");
            }
            descriptorSetLayout = descriptorSetLayoutAddress[0];
        }
    }
    
    private void createIndexBuffer() {
        List<Integer> flatIndices = new ArrayList<>();
        indices.forEach(flatIndices::addAll);
        
        long bufferSize = Integer.SIZE * flatIndices.size();
        
        long[] stagingBufferAddress = new long[] { 0 };
        long[] stagingBufferMemoryAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferAddress, stagingBufferMemoryAddress);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            
            VK10.vkMapMemory(device, stagingBufferMemoryAddress[0], 0, bufferSize, 0, data);
                IntBuffer intData = data.getIntBuffer(flatIndices.size());
                for(int index : flatIndices) {
                    intData.put(index);
                }
                intData.flip();
            VK10.vkUnmapMemory(device, stagingBufferMemoryAddress[0]);
        }
        
        long[] indexBufferAddress = new long[] { 0 };
        long[] indexBufferMemoryAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                        VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, indexBufferAddress, indexBufferMemoryAddress);
        
        indexBuffer = indexBufferAddress[0];
        indexBufferMemory = indexBufferMemoryAddress[0];
        
        copyBuffer(stagingBufferAddress[0], indexBuffer, bufferSize);
        
        VK10.vkDestroyBuffer(device, stagingBufferAddress[0], null);
        VK10.vkFreeMemory(device, stagingBufferMemoryAddress[0], null);
    }
    
    private void createVertexBuffer() {
        List<Vertex> flatVertices = new ArrayList<>();
        for(int i = 0; i < vertices.size(); i++) {
            List<Vertex> objectVertices = vertices.get(i);
            TexturePacker.AtlasMember member = texturePacker.getAtlasMember(textures.get(i) == null ? TextureList.findTexture("misc.blank") : textures.get(i));

            for (Vertex old : objectVertices) {
                Vector2f coords = new Vector2f(((((float) member.x) / texturePacker.getAtlas().getAsBufferedImage().getWidth()) + (old.texCoord.x * member.width / texturePacker.getAtlas().getAsBufferedImage().getWidth())),
                        ((((float) member.y) / texturePacker.getAtlas().getAsBufferedImage().getHeight()) + (old.texCoord.y * member.height / texturePacker.getAtlas().getAsBufferedImage().getHeight())));

                flatVertices.add(new Vertex(old.pos, old.color, coords));
            }
        }
        
        long bufferSize = (Float.BYTES * 2 + Float.BYTES * 4 + Float.BYTES * 2) * flatVertices.size(); // Vertex = (Vector2<float>, Vector4<float>, Vector2<float>));
        
        long[] stagingBufferAddress = new long[] { 0 };
        long[] stagingBufferMemoryAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferAddress, stagingBufferMemoryAddress);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            
            VK10.vkMapMemory(device, stagingBufferMemoryAddress[0], 0, bufferSize, 0, data);
                FloatBuffer floatData = data.getFloatBuffer(flatVertices.size() * (2 + 4 + 2));
                for(Vertex vertex : flatVertices) {
                    floatData
                        .put(vertex.pos.x)
                        .put(vertex.pos.y)
                        .put(vertex.color.x)
                        .put(vertex.color.y)
                        .put(vertex.color.z)
                        .put(vertex.color.w)
                        .put(vertex.texCoord.x)
                        .put(vertex.texCoord.y);
                }
                floatData.flip();
            VK10.vkUnmapMemory(device, stagingBufferMemoryAddress[0]);
        }
        
        long[] vertexBufferAddress = new long[] { 0 };
        long[] vertexBufferMemoryAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                        VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBufferAddress, vertexBufferMemoryAddress);
        
        vertexBuffer = vertexBufferAddress[0];
        vertexBufferMemory = vertexBufferMemoryAddress[0];
        
        copyBuffer(stagingBufferAddress[0], vertexBuffer, bufferSize);
        
        VK10.vkDestroyBuffer(device, stagingBufferAddress[0], null);
        VK10.vkFreeMemory(device, stagingBufferMemoryAddress[0], null);
    }
    
    private void copyBuffer(long srcBuffer, long dstBuffer, long size) {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCopy copyRegion = VkBufferCopy.mallocStack(stack)
                .srcOffset(0)
                .dstOffset(0)
                .size(size);
            VK10.vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, VkBufferCopy.mallocStack(1, stack).put(copyRegion).flip());
        }
        
        endSingleTimeCommands(commandBuffer);
    }
    
    
    private VkCommandBuffer beginSingleTimeCommands() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandPool(commandPool)
                .commandBufferCount(1);
            
            PointerBuffer commandBuffersBuffer = stack.mallocPointer(1);
            VK10.vkAllocateCommandBuffers(device, allocInfo, commandBuffersBuffer);
            
            VkCommandBuffer[] commandBuffers = new VkCommandBuffer[commandBuffersBuffer.capacity()];
            
            int i = 0;
            while(commandBuffersBuffer.hasRemaining()) {
                commandBuffers[i++] = new VkCommandBuffer(commandBuffersBuffer.get(), device);
            }
            
            VkCommandBuffer commandBuffer = commandBuffers[0];
            
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                .flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            
            VK10.vkBeginCommandBuffer(commandBuffer, beginInfo);
            
            return commandBuffer;
        }
    }
    
    private void endSingleTimeCommands(VkCommandBuffer commandBuffer) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK10.vkEndCommandBuffer(commandBuffer);
            
            PointerBuffer commandBufferBuffer = stack.mallocPointer(1).put(commandBuffer.address()).flip();
            
            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .pCommandBuffers(commandBufferBuffer);
            
            VK10.vkQueueSubmit(graphicsQueue, submitInfo, VK10.VK_NULL_HANDLE);
            VK10.vkQueueWaitIdle(graphicsQueue);
            
            VK10.vkFreeCommandBuffers(device, commandPool, commandBufferBuffer);
        }
    }
    
    private void createBuffer(long size, int usage, int memoryProperties, long[] vertexBuffer, long[] vertexBufferMemory) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                .size(size)
                .usage(usage)
                .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE);
            
            if(VK10.vkCreateBuffer(device, bufferInfo, null, vertexBuffer) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create vertex buffer!");
            }
            
            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            VK10.vkGetBufferMemoryRequirements(device, vertexBuffer[0], memRequirements);
            
            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO)
                .allocationSize(memRequirements.size())
                .memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), memoryProperties));
            
            if(VK10.vkAllocateMemory(device, allocInfo, null, vertexBufferMemory) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to allocate vertex buffer memory!");
            }
            
            VK10.vkBindBufferMemory(device, vertexBuffer[0], vertexBufferMemory[0], 0);
        }
    }
    
    private int findMemoryType(int typeFilter, int properties) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack(stack);
            VK10.vkGetPhysicalDeviceMemoryProperties(physicalDevice, memProperties);
            
            for(int i = 0; i < memProperties.memoryTypeCount(); i++) {
                if((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                    return i;
                }
            }
            
            throw new RuntimeException("failed to find suitable memory type!");
        }
    }
    
    private void cleanupSwapChain() {
        for (long swapChainFramebuffer : swapChainFramebuffers) {
            VK10.vkDestroyFramebuffer(device, swapChainFramebuffer, null);
        }
        
        swapChainExtent.free();
        
        cleanupCommandBuffers();
        
        VK10.vkDestroyPipeline(device, graphicsPipeline, null);
        VK10.vkDestroyPipelineLayout(device, pipelineLayout, null);
        VK10.vkDestroyRenderPass(device, renderPass, null);
        for (long swapChainImageView : swapChainImageViews) {
            VK10.vkDestroyImageView(device, swapChainImageView, null);
        }
        
        KHRSwapchain.vkDestroySwapchainKHR(device, swapChain, null);
    }
    
    public void recreateSwapChain() {
        VK10.vkDeviceWaitIdle(device);
        
        cleanupSwapChain();
        
        createSwapChain();
        createImageViews();
        createRenderPass();
        createGraphicsPipeline();
        createFramebuffers();
        createCommandBuffers();
    }
    
    private void cleanupCommandBuffers() {
        VK10.vkDeviceWaitIdle(device);
        
        if(commandBuffers != null) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer commandBuffersBuffer = stack.mallocPointer(commandBuffers.length);
                for(VkCommandBuffer commandBuffer : commandBuffers) {
                    commandBuffersBuffer.put(commandBuffer.address());
                }
                commandBuffersBuffer.flip();
                VK10.vkFreeCommandBuffers(device, commandPool, commandBuffersBuffer);
            }
        }
    }
    
    private void createSynchronizationPrimitives() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            
            long[] imageAvailableFenceAddress = new long[] { 0 };
            if(VK10.vkCreateFence(device, fenceInfo, null, imageAvailableFenceAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create fence!");
            }
            imageAvailableFence = imageAvailableFenceAddress[0];
            
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);
            
            long[] renderFinishedSemaphoreAddress = new long[] { 0 };
            if(VK10.vkCreateSemaphore(device, semaphoreInfo, null, renderFinishedSemaphoreAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create semaphore!");
            }
            renderFinishedSemaphore = renderFinishedSemaphoreAddress[0];
        }
    }
    
    private void createCommandBuffers() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            commandBuffers = new VkCommandBuffer[swapChainFramebuffers.length];
            
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                .commandPool(commandPool)
                .level(VK10.VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                .commandBufferCount(commandBuffers.length);
            
            PointerBuffer commandBuffersBuffer = stack.mallocPointer(commandBuffers.length);
            if(VK10.vkAllocateCommandBuffers(device, allocInfo, commandBuffersBuffer) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to allocate command buffers!");
            }
            
            int i = 0;
            while(commandBuffersBuffer.hasRemaining()) {
                commandBuffers[i++] = new VkCommandBuffer(commandBuffersBuffer.get(), device);
            }
            
            for(int j = 0; j < commandBuffers.length; j++) {
                VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(VK10.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT)
                    .pInheritanceInfo(null);
                
                VK10.vkBeginCommandBuffer(commandBuffers[j], beginInfo);
                
                VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO)
                    .renderPass(renderPass)
                    .framebuffer(swapChainFramebuffers[j])
                    .renderArea(VkRect2D.mallocStack(stack)
                        .offset(VkOffset2D.callocStack(stack).set(0, 0))
                        .extent(swapChainExtent));
                
                VkClearValue clearColor = VkClearValue.mallocStack(stack)
                    .color(VkClearColorValue.mallocStack(stack)
                        .float32(0, red)
                        .float32(1, green)
                        .float32(2, blue)
                        .float32(3, alpha));
                
                renderPassInfo.pClearValues(VkClearValue.mallocStack(1, stack).put(clearColor).flip());
                
                VK10.vkCmdBeginRenderPass(commandBuffers[j], renderPassInfo, VK10.VK_SUBPASS_CONTENTS_INLINE);
                
                VK10.vkCmdBindPipeline(commandBuffers[j], VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);
                
                LongBuffer vertexBuffers = stack.mallocLong(1).put(vertexBuffer);
                vertexBuffers.flip();
                
                LongBuffer offsets = stack.mallocLong(1).put(0);
                offsets.flip();
                
                VK10.vkCmdBindVertexBuffers(commandBuffers[j], 0, vertexBuffers, offsets);
                
                VK10.vkCmdBindIndexBuffer(commandBuffers[j], indexBuffer, 0, VK10.VK_INDEX_TYPE_UINT32);
                
                int index = 0;
                for(int k = 0; k < matrices.size(); k++) {
                    int listOffset = 0;
                    for(int l = 0; l < k; l++) {
                        listOffset += matrices.get(l).size();
                    }
                    
                    for(int l = 0; l < matrices.get(k).size(); l++) {
                        int dynamicOffset = (listOffset + l) * (int)dynamicAlignment;
                        
                        VK10.vkCmdBindDescriptorSets(commandBuffers[j], VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, new long[] { descriptorSet }, new int[] { dynamicOffset });
                        
                        VK10.vkCmdDrawIndexed(commandBuffers[j], indices.get(k).size(), 1, index, 0, 0);
                    }
                    
                    index += indices.get(k).size();
                }
                
                VK10.vkCmdEndRenderPass(commandBuffers[j]);
                
                if(VK10.vkEndCommandBuffer(commandBuffers[j]) != VK10.VK_SUCCESS) {
                    throw new RuntimeException("failed to record command buffer!");
                }
            }
        }
    }
    
    private void createCommandPool() {
        QueueFamilyIndices queueFamilyIndices = findQueueFamilies(physicalDevice);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                .queueFamilyIndex(queueFamilyIndices.graphicsFamily)
                .flags(0);
            
            long[] commandPoolAddress = new long[] { 0 };
            if(VK10.vkCreateCommandPool(device, poolInfo, null, commandPoolAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create command pool!");
            }
            commandPool = commandPoolAddress[0];
        }
    }
    
    private void createFramebuffers() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            swapChainFramebuffers = new long[swapChainImages.length];
            
            for(int i = 0; i < swapChainImageViews.length; i++) {
                LongBuffer attachments = stack.mallocLong(1).put(new long[] {
                    swapChainImageViews[i]
                });
                
                attachments.flip();
                
                VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .renderPass(renderPass)
                    .pAttachments(attachments)
                    .width(swapChainExtent.width())
                    .height(swapChainExtent.height())
                    .layers(1);
                
                long[] framebuffer = new long[] { 0 };
                if(VK10.vkCreateFramebuffer(device, framebufferInfo, null, framebuffer) != VK10.VK_SUCCESS) {
                    throw new RuntimeException("failed to create framebuffer!");
                }
                swapChainFramebuffers[i] = framebuffer[0];
            }
        }
    }
    
    private void createRenderPass() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkAttachmentDescription colorAttachment = VkAttachmentDescription.callocStack(stack)
                .format(swapChainImageFormat)
                .samples(VK10.VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
                .finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
            
            VkAttachmentReference colorAttachmentRef = VkAttachmentReference.callocStack(stack)
                .attachment(0)
                .layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            
            VkSubpassDescription subpass = VkSubpassDescription.callocStack(stack)
                .pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(1)
                .pColorAttachments(VkAttachmentReference.mallocStack(1, stack).put(colorAttachmentRef).flip());
            
            VkSubpassDependency dependency = VkSubpassDependency.callocStack(stack)
                .srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
                .dstSubpass(0)
                .srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                .srcAccessMask(0)
                .dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
                .dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
            
            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO)
                .pAttachments(VkAttachmentDescription.mallocStack(1, stack).put(colorAttachment).flip())
                .pSubpasses(VkSubpassDescription.mallocStack(1, stack).put(subpass).flip())
                .pDependencies(VkSubpassDependency.mallocStack(1, stack).put(dependency).flip());
            
            long[] renderPassAddress = new long[] { 0 };
            if(VK10.vkCreateRenderPass(device, renderPassInfo, null, renderPassAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create render pass!");
            }
            renderPass = renderPassAddress[0];
        }
    }
    
    private void createGraphicsPipeline() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            vertShaderModule = ShaderList.findShader("defaults.defaultVertShader").getShaderAddress();
            fragShaderModule = ShaderList.findShader("defaults.defaultFragShader").getShaderAddress();
            
            VkPipelineShaderStageCreateInfo vertShaderStageInfo = VkPipelineShaderStageCreateInfo.callocStack(stack)
               .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
               .stage(VK10.VK_SHADER_STAGE_VERTEX_BIT)
               .module(vertShaderModule)
               .pName(stack.UTF8("main"));
            
            VkPipelineShaderStageCreateInfo fragShaderStageInfo = VkPipelineShaderStageCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO)
                .stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
                .module(fragShaderModule)
                .pName(stack.UTF8("main"));
            
            VkPipelineShaderStageCreateInfo[] shaderStages = new VkPipelineShaderStageCreateInfo[] {
                vertShaderStageInfo, fragShaderStageInfo
            };
            
            VkVertexInputAttributeDescription[] attributeArray = Vertex.getAttributeDescriptions(stack);
            VkVertexInputAttributeDescription.Buffer attributeBuffer = VkVertexInputAttributeDescription.callocStack(attributeArray.length, stack);
            for(VkVertexInputAttributeDescription attribute : attributeArray) {
                attributeBuffer.put(attribute);
            }
            attributeBuffer.flip();
            
            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO)
                .pVertexBindingDescriptions(VkVertexInputBindingDescription.callocStack(1, stack).put(Vertex.getBindingDescription(stack)).flip())
                .pVertexAttributeDescriptions(attributeBuffer);
            
            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO)
                .topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                .primitiveRestartEnable(false);
            
            VkViewport viewport = VkViewport.mallocStack(stack)
                .x(0.0f)
                .y(0.0f)
                .width(swapChainExtent.width())
                .height(swapChainExtent.height())
                .minDepth(0.0f)
                .maxDepth(1.0f);
            
            VkRect2D sissor = VkRect2D.mallocStack(stack)
                .offset(VkOffset2D.mallocStack(stack).set(0, 0))
                .extent(swapChainExtent);
            
            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO)
                .pViewports(VkViewport.mallocStack(1, stack).put(viewport).flip())
                .pScissors(VkRect2D.mallocStack(1, stack).put(sissor).flip());
            
            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO)
                .depthClampEnable(false)
                .rasterizerDiscardEnable(false)
                .polygonMode(VK10.VK_POLYGON_MODE_FILL)
                .lineWidth(1.0f)
                .cullMode(VK10.VK_CULL_MODE_BACK_BIT)
                .frontFace(VK10.VK_FRONT_FACE_COUNTER_CLOCKWISE)
                .depthBiasEnable(false)
                .depthBiasConstantFactor(0.0f)
                .depthBiasClamp(0.0f)
                .depthBiasSlopeFactor(0.0f);
            
            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO)
                .sampleShadingEnable(false)
                .rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT)
                .minSampleShading(1.0f)
                .pSampleMask(null)
                .alphaToCoverageEnable(false)
                .alphaToOneEnable(false);
            
            VkPipelineColorBlendAttachmentState colorBlendAttachmentState = VkPipelineColorBlendAttachmentState.mallocStack(stack)
                .colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT)
                .blendEnable(true)
                .srcColorBlendFactor(VK10.VK_BLEND_FACTOR_SRC_ALPHA)
                .dstColorBlendFactor(VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
                .colorBlendOp(VK10.VK_BLEND_OP_ADD)
                .srcAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ONE)
                .dstAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ZERO)
                .alphaBlendOp(VK10.VK_BLEND_OP_ADD);
            
            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO)
                .logicOpEnable(false)
                .logicOp(VK10.VK_LOGIC_OP_COPY)
                .pAttachments(VkPipelineColorBlendAttachmentState.mallocStack(1, stack).put(colorBlendAttachmentState).flip())
                .blendConstants(0, 0.0f)
                .blendConstants(1, 0.0f)
                .blendConstants(2, 0.0f)
                .blendConstants(3, 0.0f);
            
            IntBuffer dynamicStates = stack.mallocInt(2).put(new int[] { VK10.VK_DYNAMIC_STATE_STENCIL_WRITE_MASK, VK10.VK_DYNAMIC_STATE_LINE_WIDTH });
            dynamicStates.flip();
            
            VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_DYNAMIC_STATE_CREATE_INFO)
                .pDynamicStates(dynamicStates);
            
            LongBuffer layouts = stack.mallocLong(1).put(descriptorSetLayout);
            layouts.flip();
            
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO)
                .pSetLayouts(layouts)
                .pPushConstantRanges(null);
            
            long[] pipelineLayoutAddress = new long[] { 0 };
            if(VK10.vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pipelineLayoutAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create pipeline layout!");
            }
            pipelineLayout = pipelineLayoutAddress[0];
            
            VkPipelineShaderStageCreateInfo.Buffer shaderStagesBuffer =  VkPipelineShaderStageCreateInfo.mallocStack(shaderStages.length, stack);
            for(VkPipelineShaderStageCreateInfo shaderStage : shaderStages) {
                shaderStagesBuffer.put(shaderStage);
            }
            shaderStagesBuffer.flip();
            
            VkGraphicsPipelineCreateInfo pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO)
                .pStages(shaderStagesBuffer)
                .pVertexInputState(vertexInputInfo)
                .pInputAssemblyState(inputAssembly)
                .pViewportState(viewportState)
                .pRasterizationState(rasterizer)
                .pMultisampleState(multisampling)
                .pDepthStencilState(null)
                .pColorBlendState(colorBlending)
                .pDynamicState(dynamicState)
                .layout(pipelineLayout)
                .renderPass(renderPass)
                .subpass(0)
                .basePipelineHandle(VK10.VK_NULL_HANDLE)
                .basePipelineIndex(-1);
            
            long[] graphicsPipelineAddress = new long[] { 0 };
            if(VK10.vkCreateGraphicsPipelines(device, VK10.VK_NULL_HANDLE, VkGraphicsPipelineCreateInfo.mallocStack(1, stack).put(pipelineInfo).flip(), null, graphicsPipelineAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create graphics pipeline!");
            }
            graphicsPipeline = graphicsPipelineAddress[0];
        }
    }

    private long createShaderModule(ByteBuffer code) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO)
                .pCode(code);
            
            long[] shaderModuleAddress = new long[] { 0 };
            if(VK10.vkCreateShaderModule(device, createInfo, null, shaderModuleAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create shader module!");
            }
            
            return shaderModuleAddress[0];
        }
    }
    
    private void createImageViews() {
        swapChainImageViews = new long[swapChainImages.length];
        
        for(int i = 0; i < swapChainImages.length; i++) {
            swapChainImageViews[i] = createImageView(swapChainImages[i], swapChainImageFormat);
        }
    }
    
    private void createSwapChain() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            SwapChainSupportDetails swapChainSupport = querySwapChainSupport(physicalDevice, stack);
            
            VkSurfaceFormatKHR surfaceFormat = chooseSwapSurfaceFormat(swapChainSupport.formats);
            int presentMode = chooseSwapPresentMode(swapChainSupport.presentModes);
            VkExtent2D extent = chooseSwapExtent(swapChainSupport.capabilities);
            
            int imageCount = swapChainSupport.capabilities.minImageCount() + 1;
            if(swapChainSupport.capabilities.maxImageCount() > 0 && imageCount > swapChainSupport.capabilities.maxImageCount()) {
                imageCount = swapChainSupport.capabilities.maxImageCount();
            }
        
            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack)
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR)
                .surface(surface)
                .minImageCount(imageCount)
                .imageFormat(surfaceFormat.format())
                .imageColorSpace(surfaceFormat.colorSpace())
                .imageExtent(extent)
                .imageArrayLayers(1)
                .imageUsage(VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT);
            
            QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
            IntBuffer queueFamilyIndices = stack.mallocInt(2)
                .put(indices.graphicsFamily)
                .put(indices.presentFamily);
            queueFamilyIndices.flip();
            
            if(indices.graphicsFamily != indices.presentFamily) {
                createInfo
                    .imageSharingMode(VK10.VK_SHARING_MODE_CONCURRENT)
                    .pQueueFamilyIndices(queueFamilyIndices);
            }
            else {
                createInfo
                    .imageSharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
                    .pQueueFamilyIndices(null);
            }
            
            createInfo
                .preTransform(swapChainSupport.capabilities.currentTransform())
                .compositeAlpha(KHRSurface.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR)
                .presentMode(presentMode)
                .clipped(true)
                .oldSwapchain(VK10.VK_NULL_HANDLE);
            
            LongBuffer swapChainBuffer = stack.mallocLong(1);
            if(org.lwjgl.vulkan.KHRSwapchain.vkCreateSwapchainKHR(device, createInfo, null, swapChainBuffer) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create swap chain!");
            }
            swapChain = swapChainBuffer.get();
            
            int[] swapChainImageCount = new int[] { 0 };
            KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain, swapChainImageCount, null);
            
            swapChainImages = new long[swapChainImageCount[0]];
            KHRSwapchain.vkGetSwapchainImagesKHR(device, swapChain, swapChainImageCount, swapChainImages);
            
            swapChainImageFormat = surfaceFormat.format();
            swapChainExtent = extent;
            
            swapChainSupport.free();
        }
    }
    
    private void createLogicalDevice() {
        QueueFamilyIndices indices = findQueueFamilies(physicalDevice);
        
        int[] uniqueFamilies = new int[] { indices.graphicsFamily, indices.presentFamily };
        
        if(uniqueFamilies[0] == uniqueFamilies[1]) {
            uniqueFamilies = new int[] { uniqueFamilies[0] };
        }
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer queuePriority = stack.mallocFloat(1).put(1.0f);
            queuePriority.flip();
            
            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.callocStack(stack)
                .samplerAnisotropy(true);
            
            VkDeviceQueueCreateInfo.Buffer queueCreateInfoBuffer = VkDeviceQueueCreateInfo.mallocStack(uniqueFamilies.length, stack);
            
            for(int queueFamily : uniqueFamilies) {
                VkDeviceQueueCreateInfo queueCreateInfo = VkDeviceQueueCreateInfo.callocStack(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO)
                    .queueFamilyIndex(queueFamily)
                    .pQueuePriorities(queuePriority);
                
                queueCreateInfoBuffer.put(queueCreateInfo);
            } 
            
            queueCreateInfoBuffer.flip();
            
            PointerBuffer deviceExtensionsBuffer = stack.mallocPointer(deviceExtensions.length);
            for(String deviceExtension : deviceExtensions) {
                deviceExtensionsBuffer.put(stack.UTF8(deviceExtension));
            }
            deviceExtensionsBuffer.flip();
            
            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO)
                .pQueueCreateInfos(queueCreateInfoBuffer)
                .pEnabledFeatures(deviceFeatures)
                .ppEnabledExtensionNames(deviceExtensionsBuffer);
            
            if(Luminescent.DEBUG) {
                PointerBuffer validationLayerNamesBuffer = stack.mallocPointer(validationLayers.length);
                
                for(String validationLayerName : validationLayers) {
                    validationLayerNamesBuffer.put(stack.UTF8(validationLayerName));
                }
                validationLayerNamesBuffer.flip();
                
                deviceCreateInfo.ppEnabledLayerNames(validationLayerNamesBuffer);
            }
            else {
                deviceCreateInfo.ppEnabledLayerNames(null);
            }
            
            PointerBuffer devicePointer = stack.mallocPointer(1);
            if(VK10.vkCreateDevice(physicalDevice, deviceCreateInfo, null, devicePointer) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create logical device!");
            }
            device = new VkDevice(devicePointer.get(), physicalDevice, deviceCreateInfo);
            
            PointerBuffer graphicsQueuePointer = stack.mallocPointer(1);
            VK10.vkGetDeviceQueue(device, indices.graphicsFamily, 0, graphicsQueuePointer);
            graphicsQueue = new VkQueue(graphicsQueuePointer.get(), device);
            
            PointerBuffer presentQueuePointer = stack.mallocPointer(1);
            VK10.vkGetDeviceQueue(device, indices.presentFamily, 0, presentQueuePointer);
            presentQueue = new VkQueue(presentQueuePointer.get(), device);
        }
    }
    
    private void pickPhysicalDevice() {
        physicalDevice = null;
        
        int[] deviceCount = new int[] { 0 };
        VK10.vkEnumeratePhysicalDevices(instance, deviceCount, null);
        
        if(deviceCount[0] == 0)
            throw new RuntimeException("failed to find GPUs with Vulkan support!");
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer devices = stack.mallocPointer(deviceCount[0]);
            VK10.vkEnumeratePhysicalDevices(instance, deviceCount, devices);
            
            VkPhysicalDevice[] physicalDevices = new VkPhysicalDevice[deviceCount[0]];
            int i = 0;
            while(devices.hasRemaining()) {
                physicalDevices[i++] = new VkPhysicalDevice(devices.get(), instance);
            }
            
            Map<Integer, VkPhysicalDevice> candidates = new HashMap<>(deviceCount[0]); 
            
            for(VkPhysicalDevice device : physicalDevices) {
                int score = rateSuitability(device);
                candidates.put(score, device);
            }
            
            int bestScore = candidates.keySet().stream().sorted(Comparator.reverseOrder()).findFirst().get();
            if(bestScore > 0) {
                physicalDevice = candidates.get(bestScore);
            }
            
            if(physicalDevice == null) {
                throw new RuntimeException("failed to find suitable GPU!");
            }
            
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
            VK10.vkGetPhysicalDeviceProperties(physicalDevice, deviceProperties);
            
            if(Luminescent.DEBUG) {
                APIUtil.DEBUG_STREAM.println("Chose " + deviceProperties.deviceNameString() + " as physical device.");
            }
        }
    }
    
    private int rateSuitability(VkPhysicalDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.mallocStack(stack);
            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
            VK10.vkGetPhysicalDeviceProperties(device, deviceProperties);
            VK10.vkGetPhysicalDeviceFeatures(device, deviceFeatures);
            
            int score = 0;
            
            if(deviceProperties.deviceType() == VK10.VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU) {
                score += 1000;
            }
            
            score += deviceProperties.limits().maxImageDimension2D();
            
            if(!deviceFeatures.geometryShader()) {
                score = 0;
            }
            
            if(!findQueueFamilies(device).isComplete()) {
                score = 0;
            }
            
            boolean extensionsSupported = checkDeviceExtensionSupport(device);
            
            if(!extensionsSupported) {
                score = 0;
            }
            
            boolean swapChainAdequate = false;
            if(extensionsSupported) {
                SwapChainSupportDetails swapChainSupport = querySwapChainSupport(device, stack);
                swapChainAdequate = swapChainSupport.formats.length != 0 && swapChainSupport.presentModes.length != 0;
                swapChainSupport.free();
            }
            
            if(!swapChainAdequate) {
                score = 0;
            }
            
            if(!deviceFeatures.samplerAnisotropy()) {
                score = 0;
            }
            
            return score;
        }
    }
    
    private boolean checkDeviceExtensionSupport(VkPhysicalDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            int[] extensionCount = new int[] { 0 };
            VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer)null, extensionCount, null);
            
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount[0], stack);
            VK10.vkEnumerateDeviceExtensionProperties(device, (ByteBuffer)null, extensionCount, availableExtensions);
            
            Set<String> requiredExtensions = new HashSet<>(extensionCount[0]);
            Collections.addAll(requiredExtensions, deviceExtensions);
            
            while(availableExtensions.hasRemaining()) {
                requiredExtensions.remove(availableExtensions.get().extensionNameString());
            }
            
            return requiredExtensions.isEmpty();
        }
    }
    
    private QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device) {
        QueueFamilyIndices indices = new QueueFamilyIndices();
        
        int[] queueFamilyCount = new int[] { 0 };
        VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkQueueFamilyProperties.Buffer queueFamiliesBuffer = VkQueueFamilyProperties.mallocStack(queueFamilyCount[0], stack);
            VK10.vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamiliesBuffer);
            
            VkQueueFamilyProperties[] queueFamilies = new VkQueueFamilyProperties[queueFamilyCount[0]];
            int i = 0;
            while(queueFamiliesBuffer.hasRemaining()) {
                queueFamilies[i++] = queueFamiliesBuffer.get();
            }
            
            int j = 0;
            for(VkQueueFamilyProperties queueFamily : queueFamilies) {
                
                int[] presentSupport = new int[] { 0 };
                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, j, surface, presentSupport);
                
                if(queueFamily.queueCount() > 0 && (queueFamily.queueFlags() & VK10.VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = j;
                }
                
                if(queueFamily.queueCount() > 0 && queueFamily.queueCount() > 0 && presentSupport[0] == VK10.VK_TRUE) {
                    indices.presentFamily = j;
                }
                
                if(indices.isComplete()) {
                    break;
                }
            
                j++;
            }
        
            return indices;
        }
    }
    
    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR[] availableFormats) {
        if(availableFormats.length == 1 && availableFormats[0].format() == VK10.VK_FORMAT_UNDEFINED) {
            VkSurfaceFormatKHR format = VkSurfaceFormatKHR.malloc();
            
            MemoryUtil.memPutInt(format.address() + VkSurfaceFormatKHR.FORMAT, VK10.VK_FORMAT_B8G8R8A8_UNORM);
            MemoryUtil.memPutInt(format.address() + VkSurfaceFormatKHR.COLORSPACE, KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
            
            return format;
        }
        else {
            for(VkSurfaceFormatKHR availableFormat : availableFormats) {
                if(availableFormat.format() == VK10.VK_FORMAT_B8G8R8A8_UNORM
                    && availableFormat.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                    return availableFormat;
                }
            }
        }
        
        return availableFormats[0];
    }
    
    private int chooseSwapPresentMode(int[] availablePresentModes) {
        for(int availablePresentMode : availablePresentModes) {
            if(availablePresentMode == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
                return availablePresentMode;
            }
            else if(availablePresentMode == KHRSurface.VK_PRESENT_MODE_IMMEDIATE_KHR) {
                return availablePresentMode;
            }
        }
        
        return KHRSurface.VK_PRESENT_MODE_FIFO_KHR;
    }
    
    private SwapChainSupportDetails querySwapChainSupport(VkPhysicalDevice device, MemoryStack stack) {
        SwapChainSupportDetails details = new SwapChainSupportDetails(stack);
        
        KHRSurface.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, surface, details.capabilities);
        
        int[] formatCount = new int[] { 0 };
        KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, formatCount, null);
        
        if(formatCount[0] != 0) {
            VkSurfaceFormatKHR.Buffer formatsBuffer = VkSurfaceFormatKHR.mallocStack(formatCount[0], stack);
            KHRSurface.vkGetPhysicalDeviceSurfaceFormatsKHR(device, surface, formatCount, formatsBuffer);
            
            details.formats = new VkSurfaceFormatKHR[formatCount[0]];
            
            int i = 0;
            while(formatsBuffer.hasRemaining()) {
                details.formats[i++] = formatsBuffer.get();
            }
        }
        
        int[] presentModeCount = new int[] { 0 };
        KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, presentModeCount, null);
        
        if(presentModeCount[0] != 0) {
            details.presentModes = new int[presentModeCount[0]];
            KHRSurface.vkGetPhysicalDeviceSurfacePresentModesKHR(device, surface, presentModeCount, details.presentModes);
        }
        
        return details;
    }
    
    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities) {
        if(capabilities.currentExtent().width() != Integer.MAX_VALUE) {
            int width = capabilities.currentExtent().width();
            int height = capabilities.currentExtent().height();

            return VkExtent2D.malloc()
                .width(width)
                .height(height);
        }
        else {
            int width = DisplayUtils.getDisplayWidth(), height = DisplayUtils.getDisplayHeight();
            try(MemoryStack stack = MemoryStack.stackPush()) {
                return VkExtent2D.mallocStack(stack)
                    .width(Math.max(capabilities.minImageExtent().width(), Math.min(capabilities.maxImageExtent().width(), width)))
                    .height(Math.max(capabilities.minImageExtent().height(), Math.min(capabilities.maxImageExtent().height(), height)));
            }
        }
    }
    
    private class QueueFamilyIndices {
        int graphicsFamily = -1;
        int presentFamily = -1;
        
        boolean isComplete() {
            return graphicsFamily >= 0 && presentFamily >= 0;
        }
    }
    
    private static class SwapChainSupportDetails {
        final VkSurfaceCapabilitiesKHR capabilities;
        
        VkSurfaceFormatKHR[] formats;
        int[] presentModes;
        
        private SwapChainSupportDetails(MemoryStack stack) {
            capabilities = VkSurfaceCapabilitiesKHR.malloc();
        }
        
        private void free() {
            capabilities.free();
        }
    }
    
    public static class Vertex {
        final Vector2f pos;
        final Vector4f color;
        final Vector2f texCoord;
        
        public Vertex(Vector2f pos, Vector4f color, Vector2f texCoord) {
            this.pos = pos;
            this.color = color;
            this.texCoord = texCoord;
        }
        
        private static VkVertexInputBindingDescription getBindingDescription(MemoryStack stack) {

            return VkVertexInputBindingDescription.mallocStack(stack)
                .binding(0)
                 // Vertex = (Vector2<float>, Vector4<float>, Vector2<float>)
                .stride(Float.BYTES * 2 + Float.BYTES * 4 + Float.BYTES * 2)
                .inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
        }
        
        private static VkVertexInputAttributeDescription[] getAttributeDescriptions(MemoryStack stack) {
            VkVertexInputAttributeDescription[] attributeDescriptions = new VkVertexInputAttributeDescription[3];
            
            attributeDescriptions[0] = VkVertexInputAttributeDescription.mallocStack(stack)
                .binding(0)
                .location(0)
                .format(VK10.VK_FORMAT_R32G32_SFLOAT)
                .offset(0);
            
            attributeDescriptions[1] = VkVertexInputAttributeDescription.mallocStack(stack)
                .binding(0)
                .location(1)
                .format(VK10.VK_FORMAT_R32G32B32A32_SFLOAT)
                .offset(Float.BYTES * 2);
            
            attributeDescriptions[2] = VkVertexInputAttributeDescription.mallocStack(stack)
                .binding(0)
                .location(2)
                .format(VK10.VK_FORMAT_R32G32_SFLOAT)
                .offset(Float.BYTES * 6);
            
            return attributeDescriptions;
        }
    }
    
    private static class UniformBufferObjectView {
        Matrix4f projection = new Matrix4f();
        Matrix4f view = new Matrix4f();
    }
    
    private static class UniformBufferObjectModel {
        final List<Matrix4f> model = new ArrayList<>();
    }
    
    private void createInstance() {
        boolean debug = Luminescent.DEBUG;
        if(debug && !checkValidationLayerSupport())
            throw new RuntimeException("validation layers requested, but not available!");
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkApplicationInfo appInfo = VkApplicationInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_APPLICATION_INFO)
                .pApplicationName(stack.UTF8("Luminescent"))
                .applicationVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
                .pEngineName(stack.UTF8("No Engine"))
                .engineVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
                .apiVersion(VK10.VK_API_VERSION_1_0);
            
            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.callocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO)
                .pApplicationInfo(appInfo);
            
            
            createInfo.ppEnabledExtensionNames(getRequiredExtensions(stack));
            
            if(Luminescent.DEBUG) {
                PointerBuffer validationLayerNamesBuffer = stack.mallocPointer(validationLayers.length);
                
                for(String validationLayerName : validationLayers) {
                    validationLayerNamesBuffer.put(stack.UTF8(validationLayerName));
                }
                validationLayerNamesBuffer.flip();
            
                createInfo.ppEnabledLayerNames(validationLayerNamesBuffer);
            }
            else {
                createInfo.ppEnabledLayerNames(null);
            }
            
            PointerBuffer vkInstanceBuffer = stack.mallocPointer(1);
            if(VK10.vkCreateInstance(createInfo, null, vkInstanceBuffer) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create instance!");
            }
            instance = new VkInstance(vkInstanceBuffer.get(), createInfo);
            
            if(Luminescent.DEBUG) {
                int[] extensionCount = new int[] { 0 };
                
                VK10.vkEnumerateInstanceExtensionProperties((ByteBuffer)null, extensionCount, null);
                
                VkExtensionProperties.Buffer extensionProperties = new VkExtensionProperties.Buffer(stack.malloc(VkExtensionProperties.SIZEOF * extensionCount[0]));
                
                VK10.vkEnumerateInstanceExtensionProperties((ByteBuffer)null, extensionCount, extensionProperties);
                
                APIUtil.DEBUG_STREAM.println("available extensions:");
                while(extensionProperties.hasRemaining()) {
                    APIUtil.DEBUG_STREAM.print("\t" + extensionProperties.get().extensionNameString());
                }
                APIUtil.DEBUG_STREAM.println();
            }
        }
    }
    
    private void setUpDebugCallback() {
        if(!Luminescent.DEBUG) return;
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDebugReportCallbackCreateInfoEXT createInfo = VkDebugReportCallbackCreateInfoEXT.callocStack(stack)
                .sType(EXTDebugReport.VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT)
                .flags(EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT | EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT | EXTDebugReport.VK_DEBUG_REPORT_INFORMATION_BIT_EXT | EXTDebugReport.VK_DEBUG_REPORT_DEBUG_BIT_EXT | EXTDebugReport.VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT)
                .pfnCallback(debugCallback);
            
            long[] cb = new long[] { 0 };
        
            if(EXTDebugReport.vkCreateDebugReportCallbackEXT(instance, createInfo, null, cb)
                != VK10.VK_SUCCESS)
                throw new RuntimeException("failed to set up debug callback");
            
            debugCallbackAddress = cb[0];
        }
    }
    
    private void createSurface() {
        long[] surfaceAddress = new long[] { 0 };
        if(GLFWVulkan.glfwCreateWindowSurface(instance, DisplayUtils.getHandle(), null, surfaceAddress) != VK10.VK_SUCCESS)
            throw new RuntimeException("failed to create window surface!");
        surface = surfaceAddress[0]; 
    }
    
    private PointerBuffer getRequiredExtensions(MemoryStack stack) {
        PointerBuffer glfwExtensions;
        
        glfwExtensions = GLFWVulkan.glfwGetRequiredInstanceExtensions();
        
        List<ByteBuffer> otherPointers = new ArrayList<>();
        
        if(Luminescent.DEBUG) {
            otherPointers.add(stack.UTF8(EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME));
        }
        
        PointerBuffer extensions = stack.mallocPointer(glfwExtensions.limit() + otherPointers.size());
        
        extensions.put(glfwExtensions);
        
        for(ByteBuffer buffer : otherPointers)
            extensions.put(buffer);
        
        extensions.flip();
        
        if(Luminescent.DEBUG) {
            APIUtil.DEBUG_STREAM.println("used extensions:");
            for(int i = extensions.position(); i < extensions.limit(); i++) {
                APIUtil.DEBUG_STREAM.print("\t" + extensions.getStringUTF8(i));
            } 
            APIUtil.DEBUG_STREAM.println();
        }
        
        return extensions;
    }
    
    private boolean checkValidationLayerSupport() {
        int[] layerCount = new int[] { 0 };
        
        VK10.vkEnumerateInstanceLayerProperties(layerCount, null);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkLayerProperties.Buffer availableLayers = new VkLayerProperties.Buffer(stack.malloc(VkLayerProperties.SIZEOF * layerCount[0]));
            
            VK10.vkEnumerateInstanceLayerProperties(layerCount, availableLayers);
            
            for(String layerName : validationLayers) {
                boolean layerFound = false;
                
                while(availableLayers.hasRemaining()) {
                    if(layerName.equals(availableLayers.get().layerNameString())) {
                        layerFound = true;
                        break;
                    }
                }
                
                if(!layerFound)
                    return false;
            }
        }
        
        return true;
    }
    
    private void updateUniformBuffer() {
        UniformBufferObjectModel modelUBO = new UniformBufferObjectModel();
        for(List<Supplier<Matrix4f>> matrices : matrices) {
            for(Supplier<Matrix4f> matrix : matrices) {
                modelUBO.model.add(matrix.get());
            }
        }
        
        UniformBufferObjectView viewUBO = new UniformBufferObjectView();
        viewUBO.view = viewUBO.view.identity();
        viewUBO.projection = viewUBO.projection.ortho(0, DisplayUtils.getDisplayWidth(), 0, DisplayUtils.getDisplayHeight(), 1, -1, false);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(3);
        
            VK10.vkMapMemory(device, uniformViewBufferMemory, 0, 2 * 4 * 4 * Float.BYTES, 0, data);
                FloatBuffer floatData = data.getFloatBuffer(2 * 4 * 4);
                    float[] view = new float[4 * 4];
                    viewUBO.view.get(view);
                    floatData.put(view);
                    float[] proj = new float[4 * 4];
                    viewUBO.projection.get(proj);
                    floatData.put(proj);
                    floatData.flip();
            VK10.vkUnmapMemory(device, uniformViewBufferMemory);
                
            VK10.vkMapMemory(device, uniformModelBufferMemory, 0, flatSize(matrices) * dynamicAlignment, 0, data);
                ByteBuffer byteData = data.getByteBuffer((int) (flatSize(matrices) * dynamicAlignment));
                
                
                int idx = 0;
                // Put data for each instance
                for(int i = 0; i < matrices.size(); i++) {
                    for(int k = 0; k < matrices.get(i).size(); k++) {
                        // Get model data for each instance
                        float[] modelFloat = new float[4 * 4];
                        modelUBO.model.get(idx).get(modelFloat);
                        
                        int j = 0;
                        // Add matrix to mapped memory
                        for(float f : modelFloat) {
                            byteData.putFloat((int) (idx * dynamicAlignment) + j * 4, f);
                            j++;
                        }
                        
                        TexturePacker.AtlasMember tm = texturePacker.getAtlasMember(textures.get(i) == null ? TextureList.findTexture("misc.blank") : textures.get(i));
                        
                        byteData.putInt((int) (idx * dynamicAlignment) + j * 4, currentFrames.get(i).get());
                        byteData.putInt((int) (idx * dynamicAlignment) + (j + 1) * 4, frameCount.get(i));
                        byteData.putFloat((int) (idx * dynamicAlignment) + (j + 2) * 4, tm.getTexWidth());
                        byteData.putInt((int) (idx * dynamicAlignment) + (j + 3) * 4, doLighting.get(i).get() && lighting ? 1 : 0);
                        
                        idx++;
                    }
                }
                
                byteData.flip();
                
                VkMappedMemoryRange memoryRange = VkMappedMemoryRange.callocStack(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_MAPPED_MEMORY_RANGE)
                    .memory(uniformModelBufferMemory)
                    .size(flatSize(matrices) * dynamicAlignment)
                    .offset(0)
                    .pNext(VK10.VK_NULL_HANDLE);
                
                VK10.vkFlushMappedMemoryRanges(device, memoryRange);
            VK10.vkUnmapMemory(device, uniformModelBufferMemory);

            VK10.vkMapMemory(device, uniformLightsBufferMemory, 0, LightSource.LIGHTS * lightAlignment, 0, data);
                ByteBuffer lightData = data.getByteBuffer((int) (LightSource.LIGHTS * lightAlignment));
                    for(int i = 0; i < LightSource.LIGHTS; i++) {
                        float[] light;
                        if(LightSource.get(i) == null || LightSource.get(i).getRadius() == 0) {
                            light = new float[] { 0.0f, 0.0f, 0.0f };
                        }
                        else {
                            ScaledWindowCoordinates coords = new ScaledWindowCoordinates(LightSource.get(i).getCoords());
                            light = new float[]{LightSource.get(i).getScaledX(), LightSource.get(i).getScaledY(), LightSource.get(i).getScaledRadius()};
                        }

                        for(int j = 0; j < 3; j++) {
                            lightData.putFloat((int) (i * lightAlignment) + j * Float.BYTES, light[j]);
                        }
                    }
                lightData.flip();
            VK10.vkUnmapMemory(device, uniformLightsBufferMemory);
        }
    }

    private void drawFrame() {
        int[] imageIndexAddress = new int[] { 0 };
        int result = KHRSwapchain.vkAcquireNextImageKHR(device, swapChain, Long.MAX_VALUE, VK10.VK_NULL_HANDLE, imageAvailableFence, imageIndexAddress);
        imageIndex = imageIndexAddress[0];
        
        if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
            recreateSwapChain();
            return;
        }
        else if(result != VK10.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            throw new RuntimeException("failed to aquire swap chain image!");
        }
        
        VK10.vkWaitForFences(device, imageAvailableFence, true, Long.MAX_VALUE);
        VK10.vkResetFences(device, imageAvailableFence);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer signalSemaphores = stack.mallocLong(1).put(renderFinishedSemaphore);
            signalSemaphores.flip();
            
            PointerBuffer commandBufferAddresses = stack.mallocPointer(1).put(commandBuffers[imageIndex].address()).flip();
            
            VkSubmitInfo submitInfo  = VkSubmitInfo.mallocStack(stack)
                .sType(VK10.VK_STRUCTURE_TYPE_SUBMIT_INFO)
                .waitSemaphoreCount(0)
                .pWaitSemaphores(null)
                .pWaitDstStageMask(null)
                .pCommandBuffers(commandBufferAddresses)
                .pSignalSemaphores(signalSemaphores)
                .pNext(VK10.VK_NULL_HANDLE);
            
            if(VK10.vkQueueSubmit(graphicsQueue, submitInfo, VK10.VK_NULL_HANDLE) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to submit draw command buffer!");
            }
            
            LongBuffer swapChains = stack.mallocLong(1).put(swapChain);
            swapChains.flip();
            
            IntBuffer imageIndexBuffer = stack.mallocInt(1).put(imageIndex);
            imageIndexBuffer.flip();
            
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.mallocStack(stack)
                .sType(KHRSwapchain.VK_STRUCTURE_TYPE_PRESENT_INFO_KHR)
                .pWaitSemaphores(signalSemaphores)
                .swapchainCount(1)
                .pSwapchains(swapChains)
                .pImageIndices(imageIndexBuffer)
                .pResults(null)
                .pNext(VK10.VK_NULL_HANDLE);

            result = KHRSwapchain.vkQueuePresentKHR(presentQueue, presentInfo);
            
            if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR || result == KHRSwapchain.VK_SUBOPTIMAL_KHR) {
                recreateSwapChain();
            }
            else if(result != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to present swap chain image!");
            }
        }
    }
    
    private void cleanup() {
        VK10.vkDestroyShaderModule(device, vertShaderModule, null);
        VK10.vkDestroyShaderModule(device, fragShaderModule, null);
        
        VK10.vkDeviceWaitIdle(device);
        
        cleanupSwapChain();
        
        VK10.vkDestroySampler(device, textureSampler, null);
        
        VK10.vkDestroyImageView(device, textureImageView, null);
        
        VK10.vkDestroyImage(device, textureImage, null);
        
        VK10.vkFreeMemory(device, textureImageMemory, null);
        
        VK10.vkDestroyDescriptorPool(device, descriptorPool, null);
        
        VK10.vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);
        
        VK10.vkDestroyBuffer(device, uniformModelBuffer, null);
        VK10.vkFreeMemory(device, uniformModelBufferMemory, null);
        
        VK10.vkDestroyBuffer(device, uniformViewBuffer, null);
        VK10.vkFreeMemory(device, uniformViewBufferMemory, null);

        VK10.vkDestroyBuffer(device, uniformLightsBuffer, null);
        VK10.vkFreeMemory(device, uniformLightsBufferMemory, null);
        
        VK10.vkDestroyBuffer(device, indexBuffer, null);
        VK10.vkFreeMemory(device, indexBufferMemory, null);
        
        VK10.vkDestroyBuffer(device, vertexBuffer, null);
        VK10.vkFreeMemory(device, vertexBufferMemory, null);
        
        VK10.vkDestroySemaphore(device, renderFinishedSemaphore, null);
        VK10.vkDestroyFence(device, imageAvailableFence, null);
        
        VK10.vkDestroyCommandPool(device, commandPool, null);
        
        VK10.vkDestroyDevice(device, null);
        if(Luminescent.DEBUG) {
            EXTDebugReport.vkDestroyDebugReportCallbackEXT(instance, debugCallbackAddress, null);
            debugCallback.free();
        }
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        VK10.vkDestroyInstance(instance, null);
    }
    //  width, height, 1 };
    // uint32_t width;    uint32_t height;    uint32_t layers;






    /**
     * Translates a Vulkan {@code VkResult} value to a String describing the result.
     *
     * @param result
     *            the {@code VkResult} value
     *
     * @return the result description
     */
    public static String translateVulkanResult(int result) {
        switch (result) {
            // Success codes
            case VK10.VK_SUCCESS:
                return "Command successfully completed.";
            case VK10.VK_NOT_READY:
                return "A fence or query has not yet completed.";
            case VK10.VK_TIMEOUT:
                return "A wait operation has not completed in the specified time.";
            case VK10.VK_EVENT_SET:
                return "An event is signaled.";
            case VK10.VK_EVENT_RESET:
                return "An event is unsignaled.";
            case VK10.VK_INCOMPLETE:
                return "A return array was too small for the result.";
            case KHRSwapchain.VK_SUBOPTIMAL_KHR:
                return "A swapchain no longer matches the surface properties exactly, but can still be used to present to the surface successfully.";

            // Error codes
            case VK10.VK_ERROR_OUT_OF_HOST_MEMORY:
                return "A host memory allocation has failed.";
            case VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY:
                return "A device memory allocation has failed.";
            case VK10.VK_ERROR_INITIALIZATION_FAILED:
                return "Initialization of an object could not be completed for implementation-specific reasons.";
            case VK10.VK_ERROR_DEVICE_LOST:
                return "The logical or physical device has been lost.";
            case VK10.VK_ERROR_MEMORY_MAP_FAILED:
                return "Mapping of a memory object has failed.";
            case VK10.VK_ERROR_LAYER_NOT_PRESENT:
                return "A requested layer is not present or could not be loaded.";
            case VK10.VK_ERROR_EXTENSION_NOT_PRESENT:
                return "A requested extension is not supported.";
            case VK10.VK_ERROR_FEATURE_NOT_PRESENT:
                return "A requested feature is not supported.";
            case VK10.VK_ERROR_INCOMPATIBLE_DRIVER:
                return "The requested version of Vulkan is not supported by the driver or is otherwise incompatible for implementation-specific reasons.";
            case VK10.VK_ERROR_TOO_MANY_OBJECTS:
                return "Too many objects of the type have already been created.";
            case VK10.VK_ERROR_FORMAT_NOT_SUPPORTED:
                return "A requested format is not supported on this device.";
            case KHRSurface.VK_ERROR_SURFACE_LOST_KHR:
                return "A surface is no longer available.";
            case KHRSurface.VK_ERROR_NATIVE_WINDOW_IN_USE_KHR:
                return "The requested window is already connected to a VkSurfaceKHR, or to some other non-Vulkan API.";
            case KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR:
                return "A surface has changed in such a way that it is no longer compatible with the swapchain, and further presentation requests using the "
                        + "swapchain will fail. Applications must query the new surface properties and recreate their swapchain if they wish to continue" + "presenting to the surface.";
            case KHRDisplaySwapchain.VK_ERROR_INCOMPATIBLE_DISPLAY_KHR:
                return "The display used by a swapchain does not use the same presentable image layout, or is incompatible in a way that prevents sharing an" + " image.";
            case EXTDebugReport.VK_ERROR_VALIDATION_FAILED_EXT:
                return "A validation layer found an error.";
            default:
                return String.format("%s [%d]", "Unknown", Integer.valueOf(result));
        }
    }

    private RawImage readPixelsToArray() {
        boolean supportsBlit = true;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties formatProps = VkFormatProperties.mallocStack(stack);

            VK10.vkGetPhysicalDeviceFormatProperties(physicalDevice, swapChainImageFormat, formatProps);
            if((formatProps.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_BLIT_SRC_BIT) == 0) {
                supportsBlit = false;
            }

            VK10.vkGetPhysicalDeviceFormatProperties(physicalDevice, VK10.VK_FORMAT_R8G8B8A8_UNORM, formatProps);
            if((formatProps.linearTilingFeatures() & VK10.VK_FORMAT_FEATURE_BLIT_DST_BIT) == 0) {
                supportsBlit = false;
            }

            long srcImage = swapChainImages[imageIndex];
            VkImageCreateInfo imageCreateCI = VkImageCreateInfo.callocStack(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO)
                    .imageType(VK10.VK_IMAGE_TYPE_2D)
                    .format(VK10.VK_FORMAT_R8G8B8A8_UNORM)
                    .extent(VkExtent3D.mallocStack(stack)
                            .width(swapChainExtent.width())
                            .height(swapChainExtent.height())
                            .depth(1))
                    .arrayLayers(1)
                    .mipLevels(1)
                    .initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
                    .samples(VK10.VK_SAMPLE_COUNT_1_BIT)
                    .tiling(VK10.VK_IMAGE_TILING_LINEAR)
                    .usage(VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT);

            long[] dstImage = new long[] { 0 };
            VK10.vkCreateImage(device, imageCreateCI, null, dstImage);
            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            VkMemoryAllocateInfo memAllocInfo = VkMemoryAllocateInfo.callocStack(stack)
                    .sType(VK10.VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            long[] dstImageMemory = new long[] { 0 };
            VK10.vkGetImageMemoryRequirements(device, dstImage[0], memRequirements);
            memAllocInfo.allocationSize(memRequirements.size())
                    .memoryTypeIndex(findMemoryType(memRequirements.memoryTypeBits(), VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT));
            VK10.vkAllocateMemory(device, memAllocInfo, null, dstImageMemory);
            VK10.vkBindImageMemory(device, dstImage[0], dstImageMemory[0], 0);

            VkCommandBuffer copyCmd = beginSingleTimeCommands();

            transitionImageLayout(copyCmd,
                    dstImage[0],
                    0,
                    VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK10.VK_IMAGE_LAYOUT_UNDEFINED,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            transitionImageLayout(copyCmd,
                    srcImage,
                    VK10.VK_ACCESS_MEMORY_READ_BIT,
                    VK10.VK_ACCESS_TRANSFER_READ_BIT,
                    KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            if(supportsBlit) {
                VkOffset3D blitSize = VkOffset3D.mallocStack(stack)
                        .x(swapChainExtent.width())
                        .y(swapChainExtent.height())
                        .z(1);
                VkImageBlit.Buffer imageBlitRegion = VkImageBlit.callocStack(1, stack)
                        .srcSubresource(VkImageSubresourceLayers.callocStack(stack)
                            .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                            .layerCount(1))
                        .srcOffsets(1, blitSize)
                        .dstSubresource(VkImageSubresourceLayers.callocStack(stack)
                                .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                                .layerCount(1))
                        .dstOffsets(1, blitSize);

                VK10.vkCmdBlitImage(
                    copyCmd,
                        srcImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                        dstImage[0], VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        imageBlitRegion,
                        VK10.VK_FILTER_NEAREST
                );
            }
            else {
                VkImageCopy.Buffer imageCopyRegion = VkImageCopy.callocStack(1, stack)
                        .srcSubresource(VkImageSubresourceLayers.callocStack(stack)
                                .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                                .layerCount(1))
                        .dstSubresource(VkImageSubresourceLayers.callocStack(stack)
                                .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                                .layerCount(1))
                        .extent(VkExtent3D.mallocStack(stack)
                                .width(swapChainExtent.width())
                                .height(swapChainExtent.height())
                                .depth(1));

                VK10.vkCmdCopyImage(
                        copyCmd,
                        srcImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                        dstImage[0], VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        imageCopyRegion
                );
            }

            transitionImageLayout(copyCmd,
                    dstImage[0],
                    VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK10.VK_ACCESS_MEMORY_READ_BIT,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK10.VK_IMAGE_LAYOUT_GENERAL,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            transitionImageLayout(copyCmd,
                    srcImage,
                    VK10.VK_ACCESS_TRANSFER_READ_BIT,
                    VK10.VK_ACCESS_MEMORY_READ_BIT,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            endSingleTimeCommands(copyCmd);

            VkImageSubresource subResource = VkImageSubresource.mallocStack(stack)
                    .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                    .arrayLayer(0)
                    .mipLevel(0);

            VkSubresourceLayout subResourceLayout = VkSubresourceLayout.mallocStack(stack);
            VK10.vkGetImageSubresourceLayout(device, dstImage[0], subResource, subResourceLayout);

            PointerBuffer data = stack.pointers(0);
            VK10.vkMapMemory(device, dstImageMemory[0], 0, VK10.VK_WHOLE_SIZE, 0, data);

            ByteBuffer buffer = MemoryUtil.memByteBuffer(data.get() + subResourceLayout.offset(), 4*swapChainExtent.width()*swapChainExtent.height());

            ByteBuffer bufferData = MemoryUtil.memAlloc(buffer.limit());
            bufferData.put(buffer);
            bufferData.flip();

            // TODO: More robust approach
            switch (swapChainImageFormat) {
                case VK10.VK_FORMAT_B8G8R8A8_SRGB:
                case VK10.VK_FORMAT_B8G8R8A8_UNORM:
                case VK10.VK_FORMAT_B8G8R8A8_SNORM:
                    for(int i = 0; i < bufferData.limit(); i += 4) {
                        byte temp = bufferData.get(i);
                        bufferData.put(i, bufferData.get(i + 2));
                        bufferData.put(i + 2, temp);
                        // Effectively remove all transparency
                        bufferData.put(i + 3, (byte) 0xff);
                    }
            }

            VK10.vkUnmapMemory(device, dstImageMemory[0]);
            VK10.vkFreeMemory(device, dstImageMemory[0], null);
            VK10.vkDestroyImage(device, dstImage[0], null);

            return new RawImage(swapChainExtent.width(), swapChainExtent.height(), swapChainImageFormat, bufferData);
        }
    }
    
    private <T> int flatSize(List<List<T>> lists) {
        int count = 0;
        for(List<?> list : lists) {
            count += list.size();
        }
        
        return count;
    }
    
    public static void addObject(Vertex[] vertices, int[] indices, Texture texture, Supplier<Boolean> doLighting, List<Supplier<Matrix4f>> matrices) {
        addObject(vertices, indices, texture, doLighting, texture == null ? () -> 0 : texture::getCurrentFrame, matrices);
    }
    
    public static void addObject(Vertex[] vertices, int[] indices, Texture texture, Supplier<Boolean> doLighting, Supplier<Integer> currentFrame, List<Supplier<Matrix4f>> matrices) {
        int offset = 0;
        for(List<Vertex> verts : vulkanInstance.vertices) {
            offset += verts.size();
        }
        
        List<Integer> indicesList = new ArrayList<>(indices.length);
        for(int index : indices) {
            indicesList.add(index + offset);
        }
        
        vulkanInstance.vertices.add(Arrays.asList(vertices));
        vulkanInstance.indices.add(indicesList);
        vulkanInstance.textures.add(texture);
        vulkanInstance.frameCount.add(texture == null ? 1 : texture.count());
        vulkanInstance.matrices.add(matrices);
        vulkanInstance.currentFrames.add(currentFrame);
        vulkanInstance.doLighting.add(doLighting);
    }
    
    public static void constructBuffers() {
        vulkanInstance.createTextureImage();
        vulkanInstance.createTextureImageView();
        vulkanInstance.createVertexBuffer();
        vulkanInstance.createIndexBuffer();
        vulkanInstance.createUniformBuffer();
        vulkanInstance.createDescriptorPool();
        vulkanInstance.createDescriptorSet();
        vulkanInstance.createCommandBuffers();
        vulkanInstance.recreateSwapChain();
    }
    
    private void cleanupUniformBuffers() {
        VK10.vkDestroyBuffer(device, uniformModelBuffer, null);
        VK10.vkFreeMemory(device, uniformModelBufferMemory, null);
        
        VK10.vkDestroyBuffer(device, uniformViewBuffer, null);
        VK10.vkFreeMemory(device, uniformViewBufferMemory, null);

        VK10.vkDestroyBuffer(device, uniformLightsBuffer, null);
        VK10.vkFreeMemory(device, uniformLightsBufferMemory, null);
    }
    
    private void cleanupDescriptorPool() {
        VK10.vkDestroyDescriptorPool(device, descriptorPool, null);
    }
    
    public static void recreateCommandAndUniformBuffers() {
        vulkanInstance.cleanupCommandBuffers();
        vulkanInstance.cleanupDescriptorPool();
        vulkanInstance.cleanupUniformBuffers();
        
        vulkanInstance.createUniformBuffer();
        vulkanInstance.createDescriptorPool();
        vulkanInstance.createDescriptorSet();
        vulkanInstance.createCommandBuffers();
    }
    
    public static int getInstances() {
        return vulkanInstance.matrices.size();
    }
    
    @SafeVarargs
    public static void addMatrices(int index, Supplier<Matrix4f>... matrices) {
        List<Supplier<Matrix4f>> copied = new ArrayList<>();
        copied.addAll(vulkanInstance.matrices.get(index));
        copied.addAll(new ArrayList<>(Arrays.asList(matrices)));
        vulkanInstance.matrices.set(index, copied);
    }

    public static void redraw() {
        vulkanInstance.updateUniformBuffer();
        vulkanInstance.drawFrame();
    }
}
