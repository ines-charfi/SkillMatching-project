
    // Prévisualisation de la nouvelle photo
    const photoInput = document.getElementById('photoInput');
    const currentPhotoBlock = document.getElementById('currentPhotoBlock');
    const newPhotoPreview = document.getElementById('newPhotoPreview');
    const previewImg = document.getElementById('previewImg');

    if (photoInput) {
    photoInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImg.src = e.target.result;
                newPhotoPreview.style.display = 'block';
                if (currentPhotoBlock) currentPhotoBlock.style.display = 'none';
            }
            reader.readAsDataURL(file);
        } else {
            newPhotoPreview.style.display = 'none';
            if (currentPhotoBlock) currentPhotoBlock.style.display = 'block';
        }
    });
}
