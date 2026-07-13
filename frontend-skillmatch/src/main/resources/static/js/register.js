// ============================================
// ROLE SELECTION FOR REGISTRATION FORM
// Toggles between CANDIDAT and ENTREPRISE roles, shows/hides relevant fields
// and updates the 'required' attribute for input fields accordingly
// ============================================
function selectRole(role) {
    document.getElementById('selectedRole').value = role;

    const cardC = document.getElementById('cardCandidat');
    const cardE = document.getElementById('cardEntreprise');
    const candidatFields = document.getElementById('candidatFields');
    const entrepriseFields = document.getElementById('entrepriseFields');

    if (role === 'CANDIDAT') {
        // Show candidate fields and hide company fields
        cardC.classList.add('selected');
        cardE.classList.remove('selected');
        candidatFields.classList.remove('hidden-fields');
        entrepriseFields.classList.add('hidden-fields');
        // Set required attributes for candidate-specific fields
        document.getElementById('inputPrenom').required = true;
        document.getElementById('inputNom').required = true;
        document.getElementById('inputEntreprise').required = false;
    } else {
        // Show company fields and hide candidate fields
        cardE.classList.add('selected');
        cardC.classList.remove('selected');
        candidatFields.classList.add('hidden-fields');
        entrepriseFields.classList.remove('hidden-fields');
        // Set required attributes for company-specific fields
        document.getElementById('inputPrenom').required = false;
        document.getElementById('inputNom').required = false;
        document.getElementById('inputEntreprise').required = true;
    }
}

// ============================================
// SET DEFAULT ROLE TO CANDIDAT ON PAGE LOAD
// ============================================
window.onload = () => selectRole('CANDIDAT');