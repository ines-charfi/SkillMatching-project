// ============================================
// PHOTO PREVIEW ON FILE INPUT CHANGE
// Displays a preview of the selected photo and hides the current one
// ============================================
const photoInput = document.getElementById('photoInput');
const currentPhotoBlock = document.getElementById('currentPhotoBlock');
const newPhotoPreview = document.getElementById('newPhotoPreview');
const previewImg = document.getElementById('previewImg');

if (photoInput) {
    photoInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            // Read the selected file and display its preview
            const reader = new FileReader();
            reader.onload = function(e) {
                previewImg.src = e.target.result;
                newPhotoPreview.style.display = 'block';
                if (currentPhotoBlock) currentPhotoBlock.style.display = 'none';
            }
            reader.readAsDataURL(file);
        } else {
            // No file selected: hide preview and show current photo block
            newPhotoPreview.style.display = 'none';
            if (currentPhotoBlock) currentPhotoBlock.style.display = 'block';
        }
    });
}