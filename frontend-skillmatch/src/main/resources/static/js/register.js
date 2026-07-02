
    function selectRole(role) {
    document.getElementById('selectedRole').value = role;

    const cardC = document.getElementById('cardCandidat');
    const cardE = document.getElementById('cardEntreprise');
    const candidatFields = document.getElementById('candidatFields');
    const entrepriseFields = document.getElementById('entrepriseFields');

    if (role === 'CANDIDAT') {
    cardC.classList.add('selected');
    cardE.classList.remove('selected');
    candidatFields.classList.remove('hidden-fields');
    entrepriseFields.classList.add('hidden-fields');
    document.getElementById('inputPrenom').required = true;
    document.getElementById('inputNom').required = true;
    document.getElementById('inputEntreprise').required = false;
} else {
    cardE.classList.add('selected');
    cardC.classList.remove('selected');
    candidatFields.classList.add('hidden-fields');
    entrepriseFields.classList.remove('hidden-fields');
    document.getElementById('inputPrenom').required = false;
    document.getElementById('inputNom').required = false;
    document.getElementById('inputEntreprise').required = true;
}
}
    window.onload = () => selectRole('CANDIDAT');
