<template>
  <div>
    <p>
      <FileInput v-model="uploadFiles" :accept="['image/png', 'image/jpeg']" multiple />
      <button @click="uploadImagesAsync">⬆️ Hochladen</button>
    </p>
    <p>
      <button @click="loadImageGalleryAsync">🔄️ Nachladen</button>
    </p>
    <div v-if="imageGallery.images.length > 0">
        <div
          v-for="(image, index) in imageGallery.images"
          :key="index" class="image"
          :style="`background-image: url(${image.url})`"
        >
          <button @click="deleteImageAsync(index)">⛔ Löschen</button>
        </div>
    </div>
    <h1 v-else>No Images... 😢</h1>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from "vue";
import FileInput from "@/components/FileInput.vue";

interface Image {
  url: string;
  name: string;
}

interface ImageGalleryResult {
  images: Image[];
}

const uploadFiles = ref<File[]>([])

const imageGallery = ref<ImageGalleryResult>({
  images: [],
});

async function uploadImagesAsync() {
  if (uploadFiles.value.length > 0) {
    try {
      const formData = new FormData();

      uploadFiles.value.forEach((file) => {
        formData.append(`file[${file.name}]`, file);
      })

      const response = await fetch("image/upload", {
        method: "POST",
        body: formData,
      })

      if (!response.ok) {
        alert(response.statusText);
      }
    }
    catch (error) {
      alert(error);
    }
  }

  uploadFiles.value = []
  await loadImageGalleryAsync();
}

async function getImageGalleryAsync() {
  try {
    const response = await fetch("/image-gallery");

    if (response.ok) {
      return await response.json() as ImageGalleryResult;
    }

    throw new Error(response.statusText)
  }
  catch (error) {
    alert(error);

    return {
      images: [],
    };
  }
}

async function deleteImageAsync(index: number) {
  try {
    const image = imageGallery.value.images[index];
    const imageFullName = image.url.split("/").pop();
    const response = await fetch(`/image/delete/${imageFullName}`, {
      method: "DELETE",
    });

    if (!response.ok) {
      throw new Error(response.statusText);
    }
  }
  catch (error) {
    alert(error);
  }
  finally {
    await loadImageGalleryAsync()
  }
}

async function loadImageGalleryAsync() {
  imageGallery.value = await getImageGalleryAsync();
}

onMounted(loadImageGalleryAsync)
</script>

<style scoped>
.image {
  display: inline-block;
  width: 300px;
  height: 300px;
  background-size: cover;
}
</style>
