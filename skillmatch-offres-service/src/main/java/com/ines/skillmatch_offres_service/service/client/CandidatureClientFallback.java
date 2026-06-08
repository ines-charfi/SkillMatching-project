package com.ines.skillmatch_offres_service.service.client;

import org.springframework.stereotype.Component;

@Component
public class CandidatureClientFallback implements CandidatureClient {

    @Override
    public Long CountByOffreId(Long offreId) {
        // Mode secours : si le service candidature est en panne,
        // on renvoie 0 pour le nombre de candidatures reçues sur cette offre
        // afin de ne pas bloquer l'affichage des offres d'emploi.
        return 0L;
    }
}