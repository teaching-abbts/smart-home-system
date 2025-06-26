<template>
  <div>
    <button @click="loadImageGalleryAsync">🔄️</button>
    <ul v-if="imageGallery.length > 0">
      <li v-for="(imageUrl, index) in imageGallery" :key="index">
        {{ imageUrl }}
      </li>
    </ul>
    <h1 v-else>No Images... 😢</h1>
  </div>
</template>

<script lang="ts" setup>
import { onMounted, ref } from "vue";

interface ImageGalleryResult {
  imageUrls: string[];
}

const imageGallery = ref<string[]>([]);

async function getImageGalleryAsync() {
  try {
    const response = await fetch("/image-gallery");

    return await response.json() as ImageGalleryResult;
  }
  catch (error) {
    console.error(error);

    return {
      imageUrls: [],
    };
  }
}

async function loadImageGalleryAsync() {
  const response = await getImageGalleryAsync();
  imageGallery.value = response.imageUrls;
}

onMounted(loadImageGalleryAsync)
</script>
