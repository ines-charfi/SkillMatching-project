package com.ines.skillmatch_offres_service.service;

import com.ines.skillmatch_offres_service.dto.OffreDTO;
import com.ines.skillmatch_offres_service.model.Offre;
import com.ines.skillmatch_offres_service.repository.OffreRepository;
import com.ines.skillmatch_offres_service.service.client.CandidatureClient;
import com.ines.skillmatch_offres_service.service.client.EntrepriseClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OffreServiceUnitTest {

    @Mock
    private OffreRepository offreRepository;

    @Mock
    private EntrepriseClient entrepriseClient;

    @Mock
    private CandidatureClient candidatureClient;

    @InjectMocks
    private OffreService offreService;

    private Offre offreExemple;
    private OffreDTO dtoExemple;

    @BeforeEach
    void setUp() {
        offreExemple = Offre.builder()
                .id(1L)
                .entrepriseId(100L)
                .titre("Développeur Java")
                .description("Poste en CDI")
                .competencesRequises("Java,Spring")
                .niveauRequis("BAC+3")
                .salaire("45K€")
                .active(true)
                .build();

        dtoExemple = new OffreDTO();
        dtoExemple.setEntrepriseId(100L);
        dtoExemple.setTitre("Développeur Java");
        dtoExemple.setDescription("Poste en CDI");
        dtoExemple.setCompetencesRequises("Java,Spring");
        dtoExemple.setNiveauRequis("BAC+3");
        dtoExemple.setSalaire("45K€");
    }

    @Test
    void testCreate_Success() {
        when(offreRepository.save(any(Offre.class))).thenReturn(offreExemple);

        Offre result = offreService.create(dtoExemple);

        assertNotNull(result);
        assertEquals("Développeur Java", result.getTitre());
        assertTrue(result.getActive());
        verify(offreRepository, times(1)).save(any(Offre.class));
    }

    @Test
    void testUpdate_Success() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offreExemple));
        when(offreRepository.save(any(Offre.class))).thenAnswer(i -> i.getArgument(0));

        OffreDTO updateDto = new OffreDTO();
        updateDto.setTitre("Développeur Senior");
        updateDto.setDescription("Poste évolutif");
        updateDto.setCompetencesRequises("Java,Spring,Cloud");
        updateDto.setNiveauRequis("BAC+5");
        updateDto.setSalaire("55K€");

        Offre updated = offreService.update(1L, updateDto);

        assertEquals("Développeur Senior", updated.getTitre());
        assertEquals("BAC+5", updated.getNiveauRequis());
        verify(offreRepository).save(offreExemple);
    }

    @Test
    void testDelete_SoftDelete() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offreExemple));
        when(offreRepository.save(any(Offre.class))).thenAnswer(i -> i.getArgument(0));

        offreService.delete(1L);

        assertFalse(offreExemple.getActive());
        verify(offreRepository).save(offreExemple);
    }

    @Test
    void testGetById_WithEnrichment() {
        when(offreRepository.findById(1L)).thenReturn(Optional.of(offreExemple));
        // Mocks des clients Feign pour l'enrichissement
        Map<String, Object> entrepriseInfos = new HashMap<>();
        entrepriseInfos.put("nomEntreprise", "TechCorp");
        entrepriseInfos.put("logoPath", "logos/tech.png");
        when(entrepriseClient.getEntrepriseByUserId(100L)).thenReturn(entrepriseInfos);
        when(candidatureClient.CountByOffreId(1L)).thenReturn(5L);

        Offre result = offreService.getById(1L);

        assertNotNull(result);
        assertEquals("TechCorp", result.getEntrepriseNom());
        assertEquals(5L, result.getNombreCandidatures());
    }

    @Test
    void testGetById_NotFound() {
        when(offreRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> offreService.getById(99L));
    }

    @Test
    void testGetAllActive_WithEnrichment() {
        List<Offre> offres = Arrays.asList(offreExemple);
        when(offreRepository.findByActiveTrue()).thenReturn(offres);
        when(entrepriseClient.getEntrepriseByUserId(100L)).thenReturn(Map.of("nomEntreprise", "TechCorp"));
        when(candidatureClient.CountByOffreId(1L)).thenReturn(3L);

        List<Offre> result = offreService.getAllActive();

        assertEquals(1, result.size());
        assertEquals("TechCorp", result.get(0).getEntrepriseNom());
    }

    @Test
    void testGetByEntreprise() {
        when(offreRepository.findByEntrepriseIdAndActiveTrue(100L)).thenReturn(List.of(offreExemple));
        when(entrepriseClient.getEntrepriseByUserId(100L)).thenReturn(Map.of("nomEntreprise", "TechCorp"));
        when(candidatureClient.CountByOffreId(1L)).thenReturn(2L);

        List<Offre> result = offreService.getByEntreprise(100L);

        assertEquals(1, result.size());
        assertEquals("TechCorp", result.get(0).getEntrepriseNom());
    }

    @Test
    void testSearch() {
        when(offreRepository.searchOffres("Java")).thenReturn(List.of(offreExemple));
        List<Offre> result = offreService.search("Java");
        assertEquals(1, result.size());
    }

    @Test
    void testCountByEntreprise() {
        when(offreRepository.countByEntrepriseId(100L)).thenReturn(5L);
        long count = offreService.countByEntreprise(100L);
        assertEquals(5L, count);
    }

    @Test
    void testGetLatest() {
        when(offreRepository.findLatestOffres()).thenReturn(List.of(offreExemple));
        List<Offre> result = offreService.getLatest();
        assertEquals(1, result.size());
    }

    @Test
    void testCountAllOffres() {
        when(offreRepository.count()).thenReturn(10L);
        Long count = offreService.countAllOffres();
        assertEquals(10L, count);
    }
}