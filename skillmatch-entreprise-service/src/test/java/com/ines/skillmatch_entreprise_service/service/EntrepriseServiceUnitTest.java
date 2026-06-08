package com.ines.skillmatch_entreprise_service.service;

import com.ines.skillmatch_entreprise_service.dto.EntrepriseDTO;
import com.ines.skillmatch_entreprise_service.model.Entreprise;
import com.ines.skillmatch_entreprise_service.repository.EntrepriseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntrepriseServiceUnitTest {

    @Mock
    private EntrepriseRepository repository;

    @InjectMocks
    private EntrepriseService service;

    private Entreprise entrepriseExemple;

    @BeforeEach
    void setUp() {
        entrepriseExemple = Entreprise.builder()
                .id(1L)
                .userId(100L)
                .nomEntreprise("TechCorp")
                .secteur("Informatique")
                .build();
    }

    @Test
    void testInitEntreprise_WhenNotExists_ShouldSave() {
        Long userId = 100L;
        String nom = "TechCorp";

        when(repository.existsByUserId(userId)).thenReturn(false);
        when(repository.save(any(Entreprise.class))).thenReturn(entrepriseExemple);

        service.initEntreprise(userId, nom);

        verify(repository, times(1)).save(any(Entreprise.class));
    }

    @Test
    void testInitEntreprise_WhenAlreadyExists_ShouldNotSave() {
        Long userId = 100L;
        when(repository.existsByUserId(userId)).thenReturn(true);

        service.initEntreprise(userId, "Autre nom");

        verify(repository, never()).save(any());
    }

    @Test
    void testGetById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(entrepriseExemple));

        Entreprise result = service.getById(1L);

        assertNotNull(result);
        assertEquals("TechCorp", result.getNomEntreprise());
    }

    @Test
    void testGetById_NotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getById(99L));
    }

    @Test
    void testGetByUserId_Found() {
        when(repository.findByUserId(100L)).thenReturn(Optional.of(entrepriseExemple));

        Entreprise result = service.getByUserId(100L);

        assertNotNull(result);
        assertEquals("TechCorp", result.getNomEntreprise());
    }

    @Test
    void testGetByUserId_NotFound_ReturnsEmptyEntreprise() {
        when(repository.findByUserId(200L)).thenReturn(Optional.empty());

        Entreprise result = service.getByUserId(200L);

        assertNotNull(result);
        assertNull(result.getId()); // entité vide, sans builder
    }

    @Test
    void testUpdateProfil_WithLogo() throws Exception {
        Long userId = 100L;
        EntrepriseDTO dto = new EntrepriseDTO();
        dto.setNomEntreprise("NouveauNom");
        dto.setSecteur("Finance");
        dto.setDescription("Description");
        dto.setSiteWeb("https://exemple.com");
        dto.setTelephone("0123456789");
        dto.setContactEmail("contact@exemple.com");

        MockMultipartFile logo = new MockMultipartFile("logo", "logo.png", "image/png", "contenu".getBytes());

        Entreprise entrepriseExistante = Entreprise.builder().userId(userId).build();
        when(repository.findByUserId(userId)).thenReturn(Optional.of(entrepriseExistante));
        when(repository.save(any(Entreprise.class))).thenAnswer(i -> i.getArgument(0));

        Entreprise updated = service.updateProfil(userId, dto, logo);

        assertNotNull(updated);
        assertEquals("NouveauNom", updated.getNomEntreprise());
        assertNotNull(updated.getLogoPath()); // le chemin doit être généré
        verify(repository).save(any(Entreprise.class));
    }

    @Test
    void testUpdateProfil_WithoutLogo() throws Exception {
        Long userId = 100L;
        EntrepriseDTO dto = new EntrepriseDTO();
        dto.setNomEntreprise("SansLogo");

        Entreprise existante = Entreprise.builder().userId(userId).build();
        when(repository.findByUserId(userId)).thenReturn(Optional.of(existante));
        when(repository.save(any(Entreprise.class))).thenAnswer(i -> i.getArgument(0));

        Entreprise updated = service.updateProfil(userId, dto, null);

        assertEquals("SansLogo", updated.getNomEntreprise());
        assertNull(updated.getLogoPath());
    }
}