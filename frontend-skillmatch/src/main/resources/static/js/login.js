
    function selectRole(role) {
    document.getElementById('roleInput').value = role;
    document.getElementById('btnCandidat').classList.toggle('active', role === 'CANDIDAT');
    document.getElementById('btnEntreprise').classList.toggle('active', role === 'ENTREPRISE');
    document.getElementById('btnAdmin').classList.toggle('active', role === 'ADMIN');
}

    function togglePassword() {
    const input = document.getElementById('password');
    const icon = document.getElementById('eyeIcon');
    if (input.type === 'password') {
    input.type = 'text';
    icon.classList.replace('bi-eye', 'bi-eye-slash');
} else {
    input.type = 'password';
    icon.classList.replace('bi-eye-slash', 'bi-eye');
}
}

    function startLoading() {
    const btn = document.getElementById('submitBtn');
    btn.classList.add('loading');
    const spinner = btn.querySelector('.spinner-border');
    const btnText = btn.querySelector('.btn-text');
    if(spinner && btnText) {
    spinner.classList.remove('d-none');
    btnText.textContent = "Connexion...";
}
    setTimeout(() => { btn.disabled = true; }, 50);
}
