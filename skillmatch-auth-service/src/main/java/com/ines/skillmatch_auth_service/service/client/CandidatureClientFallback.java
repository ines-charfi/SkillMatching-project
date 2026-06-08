package com.ines.skillmatch_auth_service.service.client;

import org.springframework.stereotype.Component;

@Component
public class CandidatureClientFallback implements CandidatureClient {

    @Override
    public Long countAllCandidatures() {
        // En mode secours, si le service candidature est en panne,
        // on renvoie 0 pour éviter de bloquer l'affichage des stats sur le tableau de bord Admin
        return 0L;
    }
}