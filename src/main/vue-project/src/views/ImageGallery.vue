<template>
  <ul v-if="imageGallery.length > 0">
    <li v-for="(imageUrl, index) in imageGallery" :key="index">
      {{ imageUrl }}
    </li>
  </ul>
  <h1>No Images... 😢</h1>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";

const imageGallery = ref<string[]>([]);

async function getImageGalleryAsync() {
  try {
    const response = await fetch("/image-gallery");

    return await response.json() as string[];
  }
  catch (error) {
    console.error(error);

    return [];
  }
}

onMounted(async () => {
  imageGallery.value = await getImageGalleryAsync();
})
</script>
