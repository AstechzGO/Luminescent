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
import org.lwjgl.util.vma.*;
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
        vulkanInstance.recreateSwapChain();
    }
    
    public static void tick() {
        vulkanInstance.updateUniformBuffer(vulkanInstance.imageIndex);
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

    public static final class RawImage {
        private final int width, height;
        private final ByteBuffer data;

        private RawImage(int width, int height, ByteBuffer data) {
            this.width = width;
            this.height = height;
            this.data = data;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
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

    private static final int MAX_FRAMES_IN_FLIGHT = 2;

    private final String[] validationLayers = { 
        "VK_LAYER_KHRONOS_validation"
    };
    
    private final String[] deviceExtensions = {
        KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME
    };

    private final VkDebugUtilsMessengerCallbackEXT debugCallback = VkDebugUtilsMessengerCallbackEXT.create(
            (messageSeverity, messageTypes, pCallbackData, pUserData) -> {
                System.err.println("validation layer: " + VkDebugUtilsMessengerCallbackDataEXT.create(pCallbackData).pMessageString());
                return VK10.VK_FALSE;
            }
    );
    
    private long debugMessenger;
    
    private long surface;
    
    private VkInstance instance;
    
    private VkPhysicalDevice physicalDevice;
    
    private VkDevice device;

    private long allocator;

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
    
    private long[] imageAvailableSemaphores;
    private long[] renderFinishedSemaphores;
    private long[] inFlightFences;
    private long[] imagesInFlight;
    
    private final List<List<Vertex>> vertices = new ArrayList<>();
    private final List<List<Integer>> indices = new ArrayList<>();
    private final List<Texture> textures = new ArrayList<>();
    private final List<Integer> frameCount = new ArrayList<>();
    private final List<List<Supplier<Matrix4f>>> matrices = new ArrayList<>();
    private final List<Supplier<Integer>> currentFrames = new ArrayList<>();
    private final List<Supplier<Boolean>> doLighting = new ArrayList<>();
    
    private long vertexBuffer;
    private long vertexBufferAllocation;
    private long indexBuffer;
    private long indexBufferAllocation;
    private long[] uniformViewBuffers;
    private long[] uniformViewBufferAllocations;
    private long[] uniformModelBuffers;
    private long[] uniformModelBufferAllocations;
    private long[] uniformLightsBuffers;
    private long[] uniformLightsBufferAllocations;
    
    private long dynamicAlignment;
    private long lightAlignment;
    
    private long descriptorPool;
    private long[] descriptorSets;
    
    private TexturePacker texturePacker;
    private long textureImage;
    private long textureImageAllocation;
    
    private long textureImageView;
    private long textureSampler;

    private int imageIndex;
    private int currentFrame;

    private void initVulkan() {
        createInstance();
        setupDebugMessenger();
        createSurface();
        pickPhysicalDevice();
        createLogicalDevice();
        createMemoryAllocator();
        createSwapChain();
        createImageViews();
        createRenderPass();
        createDescriptorSetLayout();
        ShaderList.initShaderList();
        createGraphicsPipeline();
        createFramebuffers();
        createCommandPool();
        createTextureSampler();
        createSyncObjects();
    }

    private void createMemoryAllocator() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VmaAllocatorCreateInfo allocatorCreateInfo = VmaAllocatorCreateInfo.calloc(stack)
                .flags(device.getCapabilities().VK_KHR_dedicated_allocation ? Vma.VMA_ALLOCATOR_CREATE_KHR_DEDICATED_ALLOCATION_BIT : 0)
                .physicalDevice(physicalDevice)
                .device(device)
                .pVulkanFunctions(VmaVulkanFunctions.calloc(stack).set(instance, device))
                .instance(instance)
                .vulkanApiVersion(VK12.VK_API_VERSION_1_2);

            PointerBuffer allocatorAddress = stack.mallocPointer(1);
            if(Vma.vmaCreateAllocator(allocatorCreateInfo, allocatorAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create memory allocator!");
            }

            allocator = allocatorAddress.get();
        }
    }
    
    private void createTextureSampler() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc(stack);
            VK10.vkGetPhysicalDeviceProperties(physicalDevice, properties);

            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack)
                .sType$Default()
                .magFilter(VK10.VK_FILTER_NEAREST)
                .minFilter(VK10.VK_FILTER_NEAREST)
                .addressModeU(VK10.VK_SAMPLER_MIPMAP_MODE_NEAREST)
                .addressModeV(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .addressModeW(VK10.VK_SAMPLER_ADDRESS_MODE_REPEAT)
                .anisotropyEnable(true)
                .maxAnisotropy(properties.limits().maxSamplerAnisotropy())
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
        textureImageView = createImageView(textureImage, VK10.VK_FORMAT_R8G8B8A8_SRGB);
    }
    
    private long createImageView(long image, int format) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack)
                .sType$Default()
                .image(image)
                .viewType(VK10.VK_IMAGE_VIEW_TYPE_2D)
                .format(format)
                .subresourceRange(VkImageSubresourceRange.calloc(stack)
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
            Texture texture = texturePacker.getAtlas();
            
            ByteBuffer pixels = texture.getAsByteBuffer();
            int width = texture.getAsBufferedImage().getWidth();
            int height = texture.getAsBufferedImage().getHeight();
            int imageSize = width * height * 4;
            
            long[] stagingBufferAddress = { 0 };
            long[] stagingBufferAllocationAddress = { 0 };
            
            createBuffer(imageSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferAddress, stagingBufferAllocationAddress);

            PointerBuffer data = stack.mallocPointer(1);
            Vma.vmaMapMemory(allocator, stagingBufferAllocationAddress[0], data);
                ByteBuffer buffer = data.getByteBuffer(imageSize).put(pixels);
                buffer.flip();
            Vma.vmaUnmapMemory(allocator, stagingBufferAllocationAddress[0]);
            
            long[] textureImageAddress = new long[] { 0 };
            long[] textureImageAllocationAddress = new long[] { 0 };
            createImage(width, height, VK10.VK_FORMAT_R8G8B8A8_SRGB, VK10.VK_IMAGE_TILING_OPTIMAL,
                    VK10.VK_IMAGE_LAYOUT_UNDEFINED,
                    VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK10.VK_IMAGE_USAGE_SAMPLED_BIT, VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    textureImageAddress, textureImageAllocationAddress);
            textureImage = textureImageAddress[0];
            textureImageAllocation = textureImageAllocationAddress[0];

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();
            transitionImageLayout(commandBuffer, textureImage, 0, VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
            endSingleTimeCommands(commandBuffer);
                copyBufferToImage(stagingBufferAddress[0], textureImage, width, height);
            commandBuffer = beginSingleTimeCommands();
            transitionImageLayout(commandBuffer, textureImage, VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_SHADER_READ_BIT,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);
            endSingleTimeCommands(commandBuffer);
            
            Vma.vmaDestroyBuffer(allocator, stagingBufferAddress[0], stagingBufferAllocationAddress[0]);
        }
    }
    
    private void transitionImageLayout(VkCommandBuffer cmdBuffer, long image, int srcAccessMask, int dstAccessMask, int oldLayout, int newLayout, int srcStageMask, int dstStageMask) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier barrier = VkImageMemoryBarrier.calloc(stack)
                .sType$Default()
                .oldLayout(oldLayout)
                .newLayout(newLayout)
                .srcQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
                .dstQueueFamilyIndex(VK10.VK_QUEUE_FAMILY_IGNORED)
                .image(image)
                .subresourceRange(VkImageSubresourceRange.calloc(stack)
                    .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                    .baseMipLevel(0)
                    .levelCount(1)
                    .baseArrayLayer(0)
                    .layerCount(1))
                .srcAccessMask(srcAccessMask)
                .dstAccessMask(dstAccessMask);

            VK10.vkCmdPipelineBarrier(
                    cmdBuffer,
                    srcStageMask, dstStageMask,
                0,
                null,
                null,
                VkImageMemoryBarrier.malloc(1, stack).put(barrier).flip()
            );
        }
    }
    
    private void copyBufferToImage(long buffer, long image, int width, int height) {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferImageCopy region = VkBufferImageCopy.calloc(stack)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                
                .imageSubresource(VkImageSubresourceLayers.calloc(stack)
                   .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                   .mipLevel(0)
                   .baseArrayLayer(0)
                   .layerCount(1))
                
                .imageOffset(VkOffset3D.calloc(stack).set(0, 0, 0))
                .imageExtent(VkExtent3D.calloc(stack).set(
                    width,
                    height,
                    1
                ));
            
            VK10.vkCmdCopyBufferToImage(
                commandBuffer,
                buffer,
                image,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VkBufferImageCopy.calloc(1, stack).put(region).flip()
            );
        }
        
        endSingleTimeCommands(commandBuffer);
    }
    
    private void copyImageToBuffer(long buffer, long image, int width, int height) {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferImageCopy region = VkBufferImageCopy.calloc(stack)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                
                .imageSubresource(VkImageSubresourceLayers.calloc(stack)
                   .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                   .mipLevel(0)
                   .baseArrayLayer(0)
                   .layerCount(1))
                
                .imageOffset(VkOffset3D.calloc(stack).set(0, 0, 0))
                .imageExtent(VkExtent3D.calloc(stack).set(
                    width,
                    height,
                    1
                ));
            
            VK10.vkCmdCopyImageToBuffer(
                commandBuffer,
                image,
                VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                buffer,
                VkBufferImageCopy.calloc(1, stack).put(region).flip()
            );

        }
        
        endSingleTimeCommands(commandBuffer);
    }
    
    private void createImage(int width, int height, int format, int tiling, int initialLayout, int usage, int properties, long[] imageAddress, long[] imageAllocationAddress) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageInfo = VkImageCreateInfo.calloc(stack)
                .sType$Default()
                .imageType(VK10.VK_IMAGE_TYPE_2D)
                .extent(VkExtent3D.calloc(stack)
                    .width(width)
                    .height(height)
                    .depth(1))
                .mipLevels(1)
                .arrayLayers(1)
                .format(format)
                .tiling(tiling)
                .initialLayout(initialLayout)
                .usage(usage)
                .samples(VK10.VK_SAMPLE_COUNT_1_BIT)
                .sharingMode(VK10.VK_SHARING_MODE_EXCLUSIVE)
                .flags(0);

            VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.calloc(stack)
                    .requiredFlags(properties);

            LongBuffer wrappedImage = stack.mallocLong(1);
            PointerBuffer wrappedAllocation = stack.mallocPointer(1);
            if(Vma.vmaCreateImage(allocator, imageInfo, allocationCreateInfo, wrappedImage, wrappedAllocation, null) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create image!");
            }
            imageAddress[0] = wrappedImage.get();
            imageAllocationAddress[0] = wrappedAllocation.get();
        }
    }
    
    private void createDescriptorSets() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            long[] layoutVals = new long[swapChainImages.length];
            Arrays.fill(layoutVals, descriptorSetLayout);
            LongBuffer layouts = stack.longs(layoutVals);
            
            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.calloc(stack)
                .sType$Default()
                .descriptorPool(descriptorPool)
                .pSetLayouts(layouts);
            
            descriptorSets = new long[swapChainImages.length];
            if(VK10.vkAllocateDescriptorSets(device, allocInfo, descriptorSets) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to allocate descriptor set!");
            }

            for(int i = 0; i < swapChainImages.length; i++) {
                VkDescriptorBufferInfo.Buffer viewBufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                        .buffer(uniformViewBuffers[i])
                        .offset(0)
                        .range(2 * 4 * 4 * Float.BYTES);

                VkDescriptorBufferInfo.Buffer modelBufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                        .buffer(uniformModelBuffers[i])
                        .offset(0)
                        .range(dynamicAlignment);

                VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack)
                        .imageLayout(VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
                        .imageView(textureImageView)
                        .sampler(textureSampler);

                VkDescriptorBufferInfo.Buffer lightsBufferInfo = VkDescriptorBufferInfo.calloc(1, stack)
                        .buffer(uniformLightsBuffers[i])
                        .offset(0)
                        .range(LightSource.LIGHTS * lightAlignment);

                VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(4, stack);

                descriptorWrites.get(0)
                        .sType$Default()
                        .dstSet(descriptorSets[i])
                        .dstBinding(0)
                        .dstArrayElement(0)
                        .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(1)
                        .pBufferInfo(viewBufferInfo)
                        .pImageInfo(null)
                        .pTexelBufferView(null)
                        .pNext(VK10.VK_NULL_HANDLE);

                descriptorWrites.get(1)
                        .sType$Default()
                        .dstSet(descriptorSets[i])
                        .dstBinding(1)
                        .dstArrayElement(0)
                        .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                        .descriptorCount(1)
                        .pBufferInfo(modelBufferInfo)
                        .pImageInfo(null)
                        .pTexelBufferView(null)
                        .pNext(VK10.VK_NULL_HANDLE);

                descriptorWrites.get(2)
                        .sType$Default()
                        .dstSet(descriptorSets[i])
                        .dstBinding(2)
                        .dstArrayElement(0)
                        .descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                        .descriptorCount(1)
                        .pBufferInfo(null)
                        .descriptorCount(1)
                        .pImageInfo(imageInfo)
                        .pTexelBufferView(null)
                        .pNext(VK10.VK_NULL_HANDLE);

                descriptorWrites.get(3)
                        .sType$Default()
                        .dstSet(descriptorSets[i])
                        .dstBinding(3)
                        .dstArrayElement(0)
                        .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                        .descriptorCount(1)
                        .pBufferInfo(lightsBufferInfo)
                        .pImageInfo(null)
                        .pTexelBufferView(null)
                        .pNext(VK10.VK_NULL_HANDLE);

                VK10.vkUpdateDescriptorSets(device, descriptorWrites, null);
            }
        }
    }
    
    private void createDescriptorPool() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.malloc(4, stack);
            
            poolSizes.get(0)
                .type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(swapChainImages.length);
            poolSizes.get(1)
                .type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                .descriptorCount(swapChainImages.length);
            poolSizes.get(2)
                .type(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .descriptorCount(swapChainImages.length);
            poolSizes.get(3)
                .type(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .descriptorCount(swapChainImages.length);
            
            VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.calloc(stack)
                .sType$Default()
                .pPoolSizes(poolSizes)
                .maxSets(swapChainImages.length);
            
            long[] descriptorPoolAddress = new long[] { 0 };
            if(VK10.vkCreateDescriptorPool(device, poolInfo, null, descriptorPoolAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create descriptor pool!");
            }
            descriptorPool = descriptorPoolAddress[0];
        }
    }
    
    private void createUniformBuffers() {
        int viewBufferSize = 2 * 4 * 4 * Float.BYTES;
        
        uniformViewBuffers = new long[swapChainImages.length];
        uniformViewBufferAllocations = new long[swapChainImages.length];
        uniformModelBuffers = new long[swapChainImages.length];
        uniformModelBufferAllocations = new long[swapChainImages.length];
        uniformLightsBuffers = new long[swapChainImages.length];
        uniformLightsBufferAllocations = new long[swapChainImages.length];


        long uboAlignment = 0;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.malloc(stack);
            VK10.vkGetPhysicalDeviceProperties(physicalDevice, deviceProperties);

            uboAlignment = deviceProperties.limits().minUniformBufferOffsetAlignment();
        }

        int instanceUBOSize = (4 * 4 * Float.BYTES) + (2 * Integer.BYTES) + (Float.BYTES) + 4;
        dynamicAlignment = (instanceUBOSize / uboAlignment) * uboAlignment + ((instanceUBOSize % uboAlignment) > 0 ? uboAlignment : 0);
        long modelBufferSize = flatSize(matrices) * dynamicAlignment;

        int lightUBOSize = 3 * Float.BYTES;
        int lightUniformAlignment = 16;
        lightAlignment = (lightUBOSize / lightUniformAlignment) * lightUniformAlignment + ((lightUBOSize % lightUniformAlignment) > 0 ? lightUniformAlignment : 0);
        long lightsBufferSize = LightSource.LIGHTS * dynamicAlignment;

        for(int i = 0; i < swapChainImages.length; i++) {
            createBuffer(viewBufferSize, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, uniformViewBuffers, i, uniformViewBufferAllocations, i);
            createBuffer(modelBufferSize, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, uniformModelBuffers, i, uniformModelBufferAllocations, i);
            createBuffer(lightsBufferSize, VK10.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT, VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, uniformLightsBuffers, i, uniformLightsBufferAllocations, i);
        }
    }
    
    private void createDescriptorSetLayout() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorSetLayoutBinding viewUBOLayoutBinding = VkDescriptorSetLayoutBinding.malloc(stack)
                .binding(0)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT);
            
            VkDescriptorSetLayoutBinding modelUBOLayoutBinding = VkDescriptorSetLayoutBinding.malloc(stack)
                .binding(1)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER_DYNAMIC)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_VERTEX_BIT);
            
            VkDescriptorSetLayoutBinding samplerLayoutBinding = VkDescriptorSetLayoutBinding.malloc(stack)
                .binding(2)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding lightsUBOLayoutBinding = VkDescriptorSetLayoutBinding.malloc(stack)
                .binding(3)
                .descriptorCount(1)
                .descriptorType(VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
                .pImmutableSamplers(null)
                .stageFlags(VK10.VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.malloc(4, stack)
                .put(viewUBOLayoutBinding)
                .put(modelUBOLayoutBinding)
                .put(samplerLayoutBinding)
                .put(lightsUBOLayoutBinding).flip();
            
            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack)
                .sType$Default()
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
        long[] stagingBufferAllocationAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferAddress, stagingBufferAllocationAddress);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            
            Vma.vmaMapMemory(allocator, stagingBufferAllocationAddress[0], data);
                IntBuffer intData = data.getIntBuffer(flatIndices.size());
                for(int index : flatIndices) {
                    intData.put(index);
                }
                intData.flip();
            Vma.vmaUnmapMemory(allocator, stagingBufferAllocationAddress[0]);
        }
        
        long[] indexBufferAddress = new long[] { 0 };
        long[] indexBufferAllocationAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                        VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, indexBufferAddress, indexBufferAllocationAddress);
        
        indexBuffer = indexBufferAddress[0];
        indexBufferAllocation = indexBufferAllocationAddress[0];
        
        copyBuffer(stagingBufferAddress[0], indexBuffer, bufferSize);
        
        Vma.vmaDestroyBuffer(allocator, stagingBufferAddress[0], stagingBufferAllocationAddress[0]);
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
        long[] stagingBufferAllocationAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK10.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT, stagingBufferAddress, stagingBufferAllocationAddress);
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            
            Vma.vmaMapMemory(allocator, stagingBufferAllocationAddress[0], data);
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
            Vma.vmaUnmapMemory(allocator, stagingBufferAllocationAddress[0]);
        }
        
        long[] vertexBufferAddress = new long[] { 0 };
        long[] vertexBufferAllocationAddress = new long[] { 0 };
        
        createBuffer(bufferSize, VK10.VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK10.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                        VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, vertexBufferAddress, vertexBufferAllocationAddress);
        
        vertexBuffer = vertexBufferAddress[0];
        vertexBufferAllocation = vertexBufferAllocationAddress[0];
        
        copyBuffer(stagingBufferAddress[0], vertexBuffer, bufferSize);
        
        Vma.vmaDestroyBuffer(allocator, stagingBufferAddress[0], stagingBufferAllocationAddress[0]);
    }
    
    private void copyBuffer(long srcBuffer, long dstBuffer, long size) {
        VkCommandBuffer commandBuffer = beginSingleTimeCommands();
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCopy copyRegion = VkBufferCopy.malloc(stack)
                .srcOffset(0)
                .dstOffset(0)
                .size(size);
            VK10.vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, VkBufferCopy.malloc(1, stack).put(copyRegion).flip());
        }
        
        endSingleTimeCommands(commandBuffer);
    }
    
    
    private VkCommandBuffer beginSingleTimeCommands() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                .sType$Default()
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
            
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                .sType$Default()
                .flags(VK10.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            
            VK10.vkBeginCommandBuffer(commandBuffer, beginInfo);
            
            return commandBuffer;
        }
    }
    
    private void endSingleTimeCommands(VkCommandBuffer commandBuffer) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VK10.vkEndCommandBuffer(commandBuffer);
            
            PointerBuffer commandBufferBuffer = stack.mallocPointer(1).put(commandBuffer.address()).flip();
            
            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack)
                .sType$Default()
                .pCommandBuffers(commandBufferBuffer);
            
            VK10.vkQueueSubmit(graphicsQueue, submitInfo, VK10.VK_NULL_HANDLE);
            VK10.vkQueueWaitIdle(graphicsQueue);
            
            VK10.vkFreeCommandBuffers(device, commandPool, commandBufferBuffer);
        }
    }

    private void createBuffer(long size, int usage, int memoryProperties, long[] bufferAddress, int bufferIdx, long[] allocationAddress, int allocationIdx) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack)
                    .sType$Default()
                    .size(size)
                    .usage(usage);

            VmaAllocationCreateInfo allocInfo = VmaAllocationCreateInfo.calloc(stack)
                    .requiredFlags(memoryProperties);

            LongBuffer wrappedBuffer = stack.mallocLong(1);
            PointerBuffer wrappedAllocation = stack.mallocPointer(1);
            if(Vma.vmaCreateBuffer(allocator, bufferInfo, allocInfo, wrappedBuffer, wrappedAllocation, null) != VK10.VK_SUCCESS) {
                throw new RuntimeException("Unable to allocate buffer");
            }

            bufferAddress[bufferIdx] = wrappedBuffer.get();
            allocationAddress[allocationIdx] = wrappedAllocation.get();
        }
    }

    private void createBuffer(long size, int usage, int memoryProperties, long[] bufferAddress, long[] allocationAddress) {
        createBuffer(size, usage, memoryProperties, bufferAddress, 0, allocationAddress, 0);
    }
    
    private void cleanupSwapChain() {
        for (long swapChainFramebuffer : swapChainFramebuffers) {
            VK10.vkDestroyFramebuffer(device, swapChainFramebuffer, null);
        }
        
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
        if(DisplayUtils.getDisplayWidth() == 0 || DisplayUtils.getDisplayHeight() == 0) return;
        VK10.vkDeviceWaitIdle(device);
        
        cleanupSwapChain();
        
        createSwapChain();
        createImageViews();
        createRenderPass();
        createGraphicsPipeline();
        createFramebuffers();
        createCommandBuffers();

        int oldSize = imagesInFlight.length;
        imagesInFlight = Arrays.copyOf(imagesInFlight, swapChainImages.length);
        if (oldSize < imagesInFlight.length) {
            Arrays.fill(imagesInFlight, oldSize, imagesInFlight.length, VK10.VK_NULL_HANDLE);
        }
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

    private void createSyncObjects() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            imageAvailableSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
            renderFinishedSemaphores = new long[MAX_FRAMES_IN_FLIGHT];
            inFlightFences = new long[MAX_FRAMES_IN_FLIGHT];
            imagesInFlight = new long[swapChainImages.length];
            Arrays.fill(imagesInFlight, VK10.VK_NULL_HANDLE);
            
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack)
                .sType$Default();

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack)
                .sType$Default()
                .flags(VK10.VK_FENCE_CREATE_SIGNALED_BIT);

            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                long[] imageAvailableSemaphoreAddress = new long[] { 0 };
                long[] renderFinishedSemaphoreAddress = new long[] { 0 };
                long[] inFlightFencesAddress = new long[] { 0 };
                if (VK10.vkCreateSemaphore(device, semaphoreInfo, null, imageAvailableSemaphoreAddress) != VK10.VK_SUCCESS
                    || VK10.vkCreateSemaphore(device, semaphoreInfo, null, renderFinishedSemaphoreAddress) != VK10.VK_SUCCESS
                    || VK10.vkCreateFence(device, fenceInfo, null, inFlightFencesAddress) != VK10.VK_SUCCESS) {
                    throw new RuntimeException("failed to create synchonization objects for a frame!");
                }
                imageAvailableSemaphores[i] = imageAvailableSemaphoreAddress[0];
                renderFinishedSemaphores[i] = renderFinishedSemaphoreAddress[0];
                inFlightFences[i] = inFlightFencesAddress[0];
            }
        }
    }
    
    private void createCommandBuffers() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            commandBuffers = new VkCommandBuffer[swapChainFramebuffers.length];
            
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack)
                .sType$Default()
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
                VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack)
                    .sType$Default()
                    .flags(VK10.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT)
                    .pInheritanceInfo(null);
                
                VK10.vkBeginCommandBuffer(commandBuffers[j], beginInfo);
                
                VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack)
                    .sType$Default()
                    .renderPass(renderPass)
                    .framebuffer(swapChainFramebuffers[j])
                    .renderArea(VkRect2D.malloc(stack)
                        .offset(VkOffset2D.calloc(stack).set(0, 0))
                        .extent(swapChainExtent));
                
                VkClearValue clearColor = VkClearValue.malloc(stack)
                    .color(VkClearColorValue.malloc(stack)
                        .float32(0, red)
                        .float32(1, green)
                        .float32(2, blue)
                        .float32(3, alpha));
                
                renderPassInfo.pClearValues(VkClearValue.malloc(1, stack).put(clearColor).flip());
                
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
                        
                        VK10.vkCmdBindDescriptorSets(commandBuffers[j], VK10.VK_PIPELINE_BIND_POINT_GRAPHICS, pipelineLayout, 0, new long[] { descriptorSets[j] }, new int[] { dynamicOffset });
                        
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
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack)
                .sType$Default()
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
                
                VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack)
                    .sType$Default()
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
            VkAttachmentDescription colorAttachment = VkAttachmentDescription.calloc(stack)
                .format(swapChainImageFormat)
                .samples(VK10.VK_SAMPLE_COUNT_1_BIT)
                .loadOp(VK10.VK_ATTACHMENT_LOAD_OP_CLEAR)
                .storeOp(VK10.VK_ATTACHMENT_STORE_OP_STORE)
                .stencilLoadOp(VK10.VK_ATTACHMENT_LOAD_OP_DONT_CARE)
                .stencilStoreOp(VK10.VK_ATTACHMENT_STORE_OP_DONT_CARE)
                .initialLayout(VK10.VK_IMAGE_LAYOUT_UNDEFINED)
                .finalLayout(KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);
            
            VkAttachmentReference colorAttachmentRef = VkAttachmentReference.calloc(stack)
                .attachment(0)
                .layout(VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
            
            VkSubpassDescription subpass = VkSubpassDescription.calloc(stack)
                .pipelineBindPoint(VK10.VK_PIPELINE_BIND_POINT_GRAPHICS)
                .colorAttachmentCount(1)
                .pColorAttachments(VkAttachmentReference.malloc(1, stack).put(colorAttachmentRef).flip());
            
            VkSubpassDependency dependency = VkSubpassDependency.calloc(stack)
                .srcSubpass(VK10.VK_SUBPASS_EXTERNAL)
                .dstSubpass(0)
                .srcStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
                .srcAccessMask(0)
                .dstStageMask(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT | VK10.VK_PIPELINE_STAGE_EARLY_FRAGMENT_TESTS_BIT)
                .dstAccessMask(VK10.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT | VK10.VK_ACCESS_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT);
            
            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack)
                .sType$Default()
                .pAttachments(VkAttachmentDescription.malloc(1, stack).put(colorAttachment).flip())
                .pSubpasses(VkSubpassDescription.malloc(1, stack).put(subpass).flip())
                .pDependencies(VkSubpassDependency.malloc(1, stack).put(dependency).flip());
            
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
            
            VkPipelineShaderStageCreateInfo vertShaderStageInfo = VkPipelineShaderStageCreateInfo.calloc(stack)
               .sType$Default()
               .stage(VK10.VK_SHADER_STAGE_VERTEX_BIT)
               .module(vertShaderModule)
               .pName(stack.UTF8("main"));
            
            VkPipelineShaderStageCreateInfo fragShaderStageInfo = VkPipelineShaderStageCreateInfo.calloc(stack)
                .sType$Default()
                .stage(VK10.VK_SHADER_STAGE_FRAGMENT_BIT)
                .module(fragShaderModule)
                .pName(stack.UTF8("main"));
            
            VkPipelineShaderStageCreateInfo[] shaderStages = new VkPipelineShaderStageCreateInfo[] {
                vertShaderStageInfo, fragShaderStageInfo
            };
            
            VkVertexInputAttributeDescription[] attributeArray = Vertex.getAttributeDescriptions(stack);
            VkVertexInputAttributeDescription.Buffer attributeBuffer = VkVertexInputAttributeDescription.calloc(attributeArray.length, stack);
            for(VkVertexInputAttributeDescription attribute : attributeArray) {
                attributeBuffer.put(attribute);
            }
            attributeBuffer.flip();
            
            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack)
                .sType$Default()
                .pVertexBindingDescriptions(VkVertexInputBindingDescription.calloc(1, stack).put(Vertex.getBindingDescription(stack)).flip())
                .pVertexAttributeDescriptions(attributeBuffer);
            
            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack)
                .sType$Default()
                .topology(VK10.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST)
                .primitiveRestartEnable(false);
            
            VkViewport viewport = VkViewport.malloc(stack)
                .x(0.0f)
                .y(0.0f)
                .width(swapChainExtent.width())
                .height(swapChainExtent.height())
                .minDepth(0.0f)
                .maxDepth(1.0f);
            
            VkRect2D sissor = VkRect2D.malloc(stack)
                .offset(VkOffset2D.malloc(stack).set(0, 0))
                .extent(swapChainExtent);
            
            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack)
                .sType$Default()
                .pViewports(VkViewport.malloc(1, stack).put(viewport).flip())
                .pScissors(VkRect2D.malloc(1, stack).put(sissor).flip());
            
            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack)
                .sType$Default()
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
            
            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack)
                .sType$Default()
                .sampleShadingEnable(false)
                .rasterizationSamples(VK10.VK_SAMPLE_COUNT_1_BIT)
                .minSampleShading(1.0f)
                .pSampleMask(null)
                .alphaToCoverageEnable(false)
                .alphaToOneEnable(false);
            
            VkPipelineColorBlendAttachmentState colorBlendAttachmentState = VkPipelineColorBlendAttachmentState.malloc(stack)
                .colorWriteMask(VK10.VK_COLOR_COMPONENT_R_BIT | VK10.VK_COLOR_COMPONENT_G_BIT | VK10.VK_COLOR_COMPONENT_B_BIT | VK10.VK_COLOR_COMPONENT_A_BIT)
                .blendEnable(true)
                .srcColorBlendFactor(VK10.VK_BLEND_FACTOR_SRC_ALPHA)
                .dstColorBlendFactor(VK10.VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA)
                .colorBlendOp(VK10.VK_BLEND_OP_ADD)
                .srcAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ONE)
                .dstAlphaBlendFactor(VK10.VK_BLEND_FACTOR_ZERO)
                .alphaBlendOp(VK10.VK_BLEND_OP_ADD);
            
            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack)
                .sType$Default()
                .logicOpEnable(false)
                .logicOp(VK10.VK_LOGIC_OP_COPY)
                .pAttachments(VkPipelineColorBlendAttachmentState.malloc(1, stack).put(colorBlendAttachmentState).flip())
                .blendConstants(0, 0.0f)
                .blendConstants(1, 0.0f)
                .blendConstants(2, 0.0f)
                .blendConstants(3, 0.0f);
            
            IntBuffer dynamicStates = stack.mallocInt(2).put(new int[] { VK10.VK_DYNAMIC_STATE_STENCIL_WRITE_MASK, VK10.VK_DYNAMIC_STATE_LINE_WIDTH });
            dynamicStates.flip();
            
            VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.calloc(stack)
                .sType$Default()
                .pDynamicStates(dynamicStates);
            
            LongBuffer layouts = stack.mallocLong(1).put(descriptorSetLayout);
            layouts.flip();
            
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack)
                .sType$Default()
                .pSetLayouts(layouts)
                .pPushConstantRanges(null);
            
            long[] pipelineLayoutAddress = new long[] { 0 };
            if(VK10.vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pipelineLayoutAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create pipeline layout!");
            }
            pipelineLayout = pipelineLayoutAddress[0];
            
            VkPipelineShaderStageCreateInfo.Buffer shaderStagesBuffer =  VkPipelineShaderStageCreateInfo.malloc(shaderStages.length, stack);
            for(VkPipelineShaderStageCreateInfo shaderStage : shaderStages) {
                shaderStagesBuffer.put(shaderStage);
            }
            shaderStagesBuffer.flip();
            
            VkGraphicsPipelineCreateInfo pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(stack)
                .sType$Default()
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
            if(VK10.vkCreateGraphicsPipelines(device, VK10.VK_NULL_HANDLE, VkGraphicsPipelineCreateInfo.malloc(1, stack).put(pipelineInfo).flip(), null, graphicsPipelineAddress) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to create graphics pipeline!");
            }
            graphicsPipeline = graphicsPipelineAddress[0];
        }
    }

    private long createShaderModule(ByteBuffer code) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.calloc(stack)
                .sType$Default()
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
        
            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack)
                .sType$Default()
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
            if(KHRSwapchain.vkCreateSwapchainKHR(device, createInfo, null, swapChainBuffer) != VK10.VK_SUCCESS) {
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
            
            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.calloc(stack)
                .samplerAnisotropy(true);
            
            VkDeviceQueueCreateInfo.Buffer queueCreateInfoBuffer = VkDeviceQueueCreateInfo.malloc(uniqueFamilies.length, stack);
            
            for(int queueFamily : uniqueFamilies) {
                VkDeviceQueueCreateInfo queueCreateInfo = VkDeviceQueueCreateInfo.calloc(stack)
                    .sType$Default()
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
            
            VkDeviceCreateInfo deviceCreateInfo = VkDeviceCreateInfo.calloc(stack)
                .sType$Default()
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
            
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.malloc(stack);
            VK10.vkGetPhysicalDeviceProperties(physicalDevice, deviceProperties);
            
            if(Luminescent.DEBUG) {
                APIUtil.DEBUG_STREAM.println("Chose " + deviceProperties.deviceNameString() + " as physical device.");
            }
        }
    }
    
    private int rateSuitability(VkPhysicalDevice device) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties deviceProperties = VkPhysicalDeviceProperties.malloc(stack);
            VkPhysicalDeviceFeatures deviceFeatures = VkPhysicalDeviceFeatures.malloc(stack);
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
            
            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc(extensionCount[0], stack);
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
            VkQueueFamilyProperties.Buffer queueFamiliesBuffer = VkQueueFamilyProperties.malloc(queueFamilyCount[0], stack);
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
        for(VkSurfaceFormatKHR availableFormat : availableFormats) {
            if (availableFormat.format() == VK10.VK_FORMAT_B8G8R8A8_SRGB
                    && availableFormat.colorSpace() == KHRSurface.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
                return availableFormat;
            }
        }
        
        return availableFormats[0];
    }
    
    private int chooseSwapPresentMode(int[] availablePresentModes) {
        for(int availablePresentMode : availablePresentModes) {
            if(availablePresentMode == KHRSurface.VK_PRESENT_MODE_MAILBOX_KHR) {
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
            VkSurfaceFormatKHR.Buffer formatsBuffer = VkSurfaceFormatKHR.malloc(formatCount[0], stack);
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
            return capabilities.currentExtent();
        }
        else {
            int width = DisplayUtils.getDisplayWidth(), height = DisplayUtils.getDisplayHeight();
            try(MemoryStack stack = MemoryStack.stackPush()) {
                return VkExtent2D.malloc(stack)
                    .width(Math.max(capabilities.minImageExtent().width(), Math.min(capabilities.maxImageExtent().width(), width)))
                    .height(Math.max(capabilities.minImageExtent().height(), Math.min(capabilities.maxImageExtent().height(), height)));
            }
        }
    }
    
    private static class QueueFamilyIndices {
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

            return VkVertexInputBindingDescription.malloc(stack)
                .binding(0)
                 // Vertex = (Vector2<float>, Vector4<float>, Vector2<float>)
                .stride(Float.BYTES * 2 + Float.BYTES * 4 + Float.BYTES * 2)
                .inputRate(VK10.VK_VERTEX_INPUT_RATE_VERTEX);
        }
        
        private static VkVertexInputAttributeDescription[] getAttributeDescriptions(MemoryStack stack) {
            VkVertexInputAttributeDescription[] attributeDescriptions = new VkVertexInputAttributeDescription[3];
            
            attributeDescriptions[0] = VkVertexInputAttributeDescription.malloc(stack)
                .binding(0)
                .location(0)
                .format(VK10.VK_FORMAT_R32G32_SFLOAT)
                .offset(0);
            
            attributeDescriptions[1] = VkVertexInputAttributeDescription.malloc(stack)
                .binding(0)
                .location(1)
                .format(VK10.VK_FORMAT_R32G32B32A32_SFLOAT)
                .offset(Float.BYTES * 2);
            
            attributeDescriptions[2] = VkVertexInputAttributeDescription.malloc(stack)
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
            VkApplicationInfo appInfo = VkApplicationInfo.calloc(stack)
                .sType$Default()
                .pApplicationName(stack.UTF8("Luminescent"))
                .applicationVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
                .pEngineName(stack.UTF8("No Engine"))
                .engineVersion(VK10.VK_MAKE_VERSION(1, 0, 0))
                .apiVersion(VK12.VK_API_VERSION_1_2);

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.calloc(stack)
                .sType$Default()
                .pApplicationInfo(appInfo)
                .ppEnabledExtensionNames(getRequiredExtensions(stack));
            
            if(Luminescent.DEBUG) {
                PointerBuffer validationLayerNamesBuffer = stack.mallocPointer(validationLayers.length);

                for(String validationLayerName : validationLayers) {
                    validationLayerNamesBuffer.put(stack.UTF8(validationLayerName));
                }
                validationLayerNamesBuffer.flip();

                createInfo.ppEnabledLayerNames(validationLayerNamesBuffer);

                VkDebugUtilsMessengerCreateInfoEXT debugCreateInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
                populateDebugMessengerCreateInfo(debugCreateInfo);
                createInfo.pNext(debugCreateInfo.address());
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

    private void populateDebugMessengerCreateInfo(VkDebugUtilsMessengerCreateInfoEXT createInfo) {
        createInfo.sType$Default()
                .messageSeverity(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT
                        | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
                .messageType(EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT
                        | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT | EXTDebugUtils.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT)
                .pfnUserCallback(debugCallback);
    }

    private void setupDebugMessenger() {
        if(!Luminescent.DEBUG) return;
        
        try(MemoryStack stack = MemoryStack.stackPush()) {
            VkDebugUtilsMessengerCreateInfoEXT createInfo = VkDebugUtilsMessengerCreateInfoEXT.calloc(stack);
            populateDebugMessengerCreateInfo(createInfo);
            
            long[] cb = new long[] { 0 };
        
            if(EXTDebugUtils.vkCreateDebugUtilsMessengerEXT(instance, createInfo, null, cb)
                != VK10.VK_SUCCESS)
                throw new RuntimeException("failed to set up debug messenger!");
            
            debugMessenger = cb[0];
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
            otherPointers.add(stack.UTF8(EXTDebugUtils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME));
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
    
    private void updateUniformBuffer(int currentImage) {
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

            Vma.vmaMapMemory(allocator, uniformViewBufferAllocations[currentImage], data);
                FloatBuffer floatData = data.getFloatBuffer(2 * 4 * 4);
                    float[] view = new float[4 * 4];
                    viewUBO.view.get(view);
                    floatData.put(view);
                    float[] proj = new float[4 * 4];
                    viewUBO.projection.get(proj);
                    floatData.put(proj);
                    floatData.flip();
            Vma.vmaUnmapMemory(allocator, uniformViewBufferAllocations[currentImage]);

            Vma.vmaMapMemory(allocator, uniformModelBufferAllocations[currentImage], data);
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

                Vma.vmaFlushAllocation(allocator, uniformModelBufferAllocations[currentImage], 0, flatSize(matrices) * dynamicAlignment);
            Vma.vmaUnmapMemory(allocator, uniformModelBufferAllocations[currentImage]);

            Vma.vmaMapMemory(allocator, uniformLightsBufferAllocations[currentImage], data);
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
            Vma.vmaUnmapMemory(allocator, uniformLightsBufferAllocations[currentImage]);
        }
    }

    private void drawFrame() {
        VK10.vkWaitForFences(device, inFlightFences[currentFrame], true, Long.MAX_VALUE);

        int[] imageIndexAddress = new int[] { 0 };
        int result = KHRSwapchain.vkAcquireNextImageKHR(device, swapChain, Long.MAX_VALUE, imageAvailableSemaphores[currentFrame], VK10.VK_NULL_HANDLE, imageIndexAddress);
        imageIndex = imageIndexAddress[0];

        if(result == KHRSwapchain.VK_ERROR_OUT_OF_DATE_KHR) {
            recreateSwapChain();
            return;
        }
        else if(result != VK10.VK_SUCCESS && result != KHRSwapchain.VK_SUBOPTIMAL_KHR) {
            throw new RuntimeException("failed to aquire swap chain image!");
        }

        if(imagesInFlight[imageIndex] != VK10.VK_NULL_HANDLE) {
            VK10.vkWaitForFences(device, imagesInFlight[imageIndex], true, Long.MAX_VALUE);
        }

        imagesInFlight[imageIndex] = inFlightFences[currentFrame];


        try(MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer waitSemaphores = stack.mallocLong(1).put(imageAvailableSemaphores[currentFrame]);
            waitSemaphores.flip();

            LongBuffer signalSemaphores = stack.mallocLong(1).put(renderFinishedSemaphores[currentFrame]);
            signalSemaphores.flip();
            
            PointerBuffer commandBufferAddresses = stack.mallocPointer(1).put(commandBuffers[imageIndex].address()).flip();
            
            VkSubmitInfo submitInfo  = VkSubmitInfo.malloc(stack)
                .sType$Default()
                .waitSemaphoreCount(1)
                .pWaitSemaphores(waitSemaphores)
                .pWaitDstStageMask(stack.ints(VK10.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT))
                .pCommandBuffers(commandBufferAddresses)
                .pSignalSemaphores(signalSemaphores)
                .pNext(VK10.VK_NULL_HANDLE);

            VK10.vkResetFences(device, inFlightFences[currentFrame]);

            if(VK10.vkQueueSubmit(graphicsQueue, submitInfo, inFlightFences[currentFrame]) != VK10.VK_SUCCESS) {
                throw new RuntimeException("failed to submit draw command buffer!");
            }
            
            LongBuffer swapChains = stack.mallocLong(1).put(swapChain);
            swapChains.flip();
            
            IntBuffer imageIndexBuffer = stack.mallocInt(1).put(imageIndex);
            imageIndexBuffer.flip();
            
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.malloc(stack)
                .sType$Default()
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

            currentFrame = (currentFrame + 1) % MAX_FRAMES_IN_FLIGHT;
        }
    }
    
    private void cleanup() {
        VK10.vkDestroyShaderModule(device, vertShaderModule, null);
        VK10.vkDestroyShaderModule(device, fragShaderModule, null);
        
        VK10.vkDeviceWaitIdle(device);
        
        cleanupSwapChain();
        
        VK10.vkDestroySampler(device, textureSampler, null);
        
        VK10.vkDestroyImageView(device, textureImageView, null);
        
        Vma.vmaDestroyImage(allocator, textureImage, textureImageAllocation);
        
        VK10.vkDestroyDescriptorPool(device, descriptorPool, null);
        
        VK10.vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null);

        cleanupUniformBuffers();

        Vma.vmaDestroyBuffer(allocator, indexBuffer, indexBufferAllocation);
        Vma.vmaDestroyBuffer(allocator, vertexBuffer, vertexBufferAllocation);

        for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
            VK10.vkDestroySemaphore(device, imageAvailableSemaphores[i], null);
            VK10.vkDestroySemaphore(device, renderFinishedSemaphores[i], null);
            VK10.vkDestroyFence(device, inFlightFences[i], null);
        }
        
        VK10.vkDestroyCommandPool(device, commandPool, null);

        cleanupAllocator();

        VK10.vkDestroyDevice(device, null);
        if(Luminescent.DEBUG) {
            EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT(instance, debugMessenger, null);
        }
        KHRSurface.vkDestroySurfaceKHR(instance, surface, null);
        VK10.vkDestroyInstance(instance, null);
        if(Luminescent.DEBUG) {
            debugCallback.free();
        }
    }

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
                return String.format("%s [%d]", "Unknown", result);
        }
    }

    private RawImage readPixelsToArray() {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            boolean twoSteps, copyOnly;

            VkFormatProperties targetFormatProps = VkFormatProperties.malloc(stack);
            VK10.vkGetPhysicalDeviceFormatProperties(physicalDevice, VK10.VK_FORMAT_R8G8B8A8_SRGB, targetFormatProps);

            if(swapChainImageFormat == VK10.VK_FORMAT_R8G8B8A8_SRGB) {
                twoSteps = false;
                copyOnly = true;
            }
            else {
                boolean bltLinear = (targetFormatProps.linearTilingFeatures() & VK10.VK_FORMAT_FEATURE_BLIT_DST_BIT) != 0;
                boolean bltOptimal = (targetFormatProps.optimalTilingFeatures() & VK10.VK_FORMAT_FEATURE_BLIT_DST_BIT) != 0;

                if(!bltLinear && !bltOptimal) {
                    twoSteps = false;
                    copyOnly = true;
                }
                else if(!bltLinear) {
                    twoSteps = true;
                    copyOnly = false;
                }
                else {
                    twoSteps = false;
                    copyOnly = false;
                }
            }

            long[] firstImageAddress = new long[] { 0 };
            long[] firstImageAllocationAddress = new long[] { 0 };

            long[] secondImageAddress = new long[] { 0 };
            long[] secondImageAllocationAddress = new long[] { 0 };

            long srcImage = swapChainImages[imageIndex];

            if(twoSteps) {
                createImage(swapChainExtent.width(), swapChainExtent.height(), VK10.VK_FORMAT_R8G8B8A8_SRGB,
                        VK10.VK_IMAGE_TILING_OPTIMAL, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                        VK10.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT, firstImageAddress, firstImageAllocationAddress);

                createImage(swapChainExtent.width(), swapChainExtent.height(), VK10.VK_FORMAT_R8G8B8A8_SRGB,
                        VK10.VK_IMAGE_TILING_LINEAR, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                        VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, secondImageAddress, secondImageAllocationAddress);
            }
            else {
                createImage(swapChainExtent.width(), swapChainExtent.height(), VK10.VK_FORMAT_R8G8B8A8_SRGB,
                        VK10.VK_IMAGE_TILING_LINEAR, VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT,
                        VK10.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT, firstImageAddress, firstImageAllocationAddress);
            }

            VkCommandBuffer commandBuffer = beginSingleTimeCommands();

            transitionImageLayout(commandBuffer, srcImage, VK10.VK_ACCESS_MEMORY_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
                    KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    VK10.VK_PIPELINE_STAGE_BOTTOM_OF_PIPE_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            transitionImageLayout(commandBuffer, firstImageAddress[0], 0, VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                    VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);


            VkImageCopy.Buffer imageCopyRegion = VkImageCopy.malloc(1, stack)
                    .srcSubresource(VkImageSubresourceLayers.malloc(stack)
                        .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                        .mipLevel(0)
                        .baseArrayLayer(0)
                        .layerCount(1))
                    .srcOffset(VkOffset3D.malloc(stack)
                        .x(0)
                        .y(0)
                        .z(0))
                    .dstSubresource(VkImageSubresourceLayers.malloc(stack)
                        .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                        .mipLevel(0)
                        .baseArrayLayer(0)
                        .layerCount(1))
                    .dstOffset(VkOffset3D.malloc(stack)
                        .x(0)
                        .y(0)
                        .z(0))
                    .extent(VkExtent3D.malloc(stack)
                        .width(swapChainExtent.width())
                        .height(swapChainExtent.height())
                        .depth(1));

            if(copyOnly) {
                VK10.vkCmdCopyImage(commandBuffer, srcImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, firstImageAddress[0],
                        VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, imageCopyRegion);
            }
            else {
                VkImageBlit.Buffer imageBlitRegion = VkImageBlit.calloc(1, stack)
                        .srcSubresource(VkImageSubresourceLayers.malloc(stack)
                            .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                            .baseArrayLayer(0)
                            .mipLevel(0)
                            .layerCount(1))
                        .srcOffsets(VkOffset3D.calloc(2, stack).position(1)
                                .x(swapChainExtent.width())
                                .y(swapChainExtent.height())
                                .z(1).position(0))
                        .dstSubresource(VkImageSubresourceLayers.malloc(stack)
                                .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                                .mipLevel(0)
                                .baseArrayLayer(0)
                                .layerCount(1))
                        .dstOffsets(VkOffset3D.calloc(2, stack).position(1)
                                .x(swapChainExtent.width())
                                .y(swapChainExtent.height())
                                .z(1).position(0));

                VK10.vkCmdBlitImage(commandBuffer, srcImage, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, firstImageAddress[0],
                        VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, imageBlitRegion, VK10.VK_FILTER_NEAREST);

                if(twoSteps) {
                    transitionImageLayout(commandBuffer, secondImageAddress[0], 0, VK10.VK_ACCESS_TRANSFER_WRITE_BIT,
                            VK10.VK_IMAGE_LAYOUT_UNDEFINED, VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                            VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

                    transitionImageLayout(commandBuffer, firstImageAddress[0], VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
                            VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                            VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

                    VK10.vkCmdCopyImage(commandBuffer, firstImageAddress[0], VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, secondImageAddress[0],
                            VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, imageCopyRegion);
                }
            }

            if(twoSteps) {
                transitionImageLayout(commandBuffer, secondImageAddress[0], VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
                        VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_GENERAL,
                        VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
            }
            else {
                transitionImageLayout(commandBuffer, firstImageAddress[0], VK10.VK_ACCESS_TRANSFER_WRITE_BIT, VK10.VK_ACCESS_TRANSFER_READ_BIT,
                        VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK10.VK_IMAGE_LAYOUT_GENERAL,
                        VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);
            }

            transitionImageLayout(commandBuffer, srcImage, VK10.VK_ACCESS_TRANSFER_READ_BIT, 0,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL, KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR,
                    VK10.VK_PIPELINE_STAGE_TRANSFER_BIT, VK10.VK_PIPELINE_STAGE_TRANSFER_BIT);

            endSingleTimeCommands(commandBuffer);

            VkImageSubresource subresource = VkImageSubresource.malloc(stack)
                    .aspectMask(VK10.VK_IMAGE_ASPECT_COLOR_BIT)
                    .mipLevel(0)
                    .arrayLayer(0);
            VkSubresourceLayout subresourceLayout = VkSubresourceLayout.calloc(stack);

            PointerBuffer data = stack.mallocPointer(1);
            if(twoSteps) {
                VK10.vkGetImageSubresourceLayout(device, secondImageAddress[0], subresource, subresourceLayout);
                Vma.vmaMapMemory(allocator, secondImageAllocationAddress[0], data);
            }
            else {
                VK10.vkGetImageSubresourceLayout(device, firstImageAddress[0], subresource, subresourceLayout);
                Vma.vmaMapMemory(allocator, firstImageAllocationAddress[0], data);
            }
                ByteBuffer buffer = MemoryUtil.memByteBuffer(data.get() + subresourceLayout.offset(), 4*swapChainExtent.width()*swapChainExtent.height());

                ByteBuffer bufferData = MemoryUtil.memAlloc(3*swapChainExtent.width()*swapChainExtent.height());

                while(buffer.hasRemaining()) {
                    bufferData.put(buffer.get());
                    bufferData.put(buffer.get());
                    bufferData.put(buffer.get());
                    buffer.position(buffer.position() + 1);
                }

                bufferData.flip();
            if(twoSteps) {
                Vma.vmaUnmapMemory(allocator, secondImageAllocationAddress[0]);
            }
            else {
                Vma.vmaUnmapMemory(allocator, firstImageAllocationAddress[0]);
            }

            if(twoSteps) {
                Vma.vmaDestroyImage(allocator, secondImageAddress[0], secondImageAllocationAddress[0]);
            }
            Vma.vmaDestroyImage(allocator, firstImageAddress[0], firstImageAllocationAddress[0]);

            return new RawImage(swapChainExtent.width(), swapChainExtent.height(), bufferData);
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
        vulkanInstance.createUniformBuffers();
        vulkanInstance.createDescriptorPool();
        vulkanInstance.createDescriptorSets();
        vulkanInstance.createCommandBuffers();
        vulkanInstance.recreateSwapChain();
    }

    private void cleanupAllocator() {
        if(Luminescent.DEBUG) {
            try(MemoryStack stack = MemoryStack.stackPush()) {
                VmaStats stats = VmaStats.malloc(stack);
                Vma.vmaCalculateStats(allocator, stats);
                if(stats.total().allocationCount() != 0) {
                    System.out.println("VMA Error: " + stats.total().allocationCount() + " unfreed buffers/image(s)");
                }

            }
        }

        Vma.vmaDestroyAllocator(allocator);
    }

    private void cleanupUniformBuffers() {
        for(int i = 0; i < swapChainImages.length; i++) {
            Vma.vmaDestroyBuffer(allocator, uniformModelBuffers[i], uniformModelBufferAllocations[i]);
            Vma.vmaDestroyBuffer(allocator, uniformViewBuffers[i], uniformViewBufferAllocations[i]);
            Vma.vmaDestroyBuffer(allocator, uniformLightsBuffers[i], uniformLightsBufferAllocations[i]);
        }
    }
    
    private void cleanupDescriptorPool() {
        VK10.vkDestroyDescriptorPool(device, descriptorPool, null);
    }
    
    public static void recreateCommandAndUniformBuffers() {
        vulkanInstance.cleanupCommandBuffers();
        vulkanInstance.cleanupDescriptorPool();
        vulkanInstance.cleanupUniformBuffers();
        
        vulkanInstance.createUniformBuffers();
        vulkanInstance.createDescriptorPool();
        vulkanInstance.createDescriptorSets();
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
        vulkanInstance.updateUniformBuffer(vulkanInstance.imageIndex);
        vulkanInstance.drawFrame();
    }
}
