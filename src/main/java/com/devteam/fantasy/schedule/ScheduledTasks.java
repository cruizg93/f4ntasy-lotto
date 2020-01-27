package com.devteam.fantasy.schedule;


import com.devteam.fantasy.repository.*;
import com.devteam.fantasy.service.SorteoService;
import com.devteam.fantasy.util.ProfileIdentifier;
import com.devteam.fantasy.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ScheduledTasks {
    @Autowired
    private SorteoRepository sorteoRepository;

    @Autowired
    private SorteoDiariaRepository sorteoDiariaRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    ProfileIdentifier profileIdentifier;  
    
    @Autowired 
    private SorteoService sorteoService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);


    @Scheduled(cron = "0 59 10 * * ?")
    public void updateSorteoDiaria11() {
    	log.debug("updateSorteoDiaria11: START");
    	log.info("updateSorteoDiaria11: [CLOSE 11am]");
        try {
        	if ( profileIdentifier.isProfileProdActive()) {
				Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 11);
    		}
		} catch (Exception e) {
			log.error("ERROR CLOSE[11am]:",e);
		}
        log.debug("updateSorteoDiaria11: END");
    }

    @Scheduled(cron = "0 59 14 * * ?")
    public void updateSorteoDiaria15() {
    	log.debug("updateSorteoDiaria15: START");
    	log.info("updateSorteoDiaria15: [CLOSE 15pm]");
        try {
        	if ( profileIdentifier.isProfileProdActive()) { 
        		Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 15);
    		}
        } catch (Exception e) {
			log.error("ERROR CLOSE[15pm]:",e);
		}
        log.debug("updateSorteoDiaria15: END");

    }

    @Scheduled(cron = "0 59 20 * * ?")
    public void updateSorteoDiaria21() {
    	log.debug("updateSorteoDiaria21: START");
    	log.info("updateSorteoDiaria21: [CLOSE 21pm]");
        try {
        	if ( profileIdentifier.isProfileProdActive()) {
        		Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 21);
    		}
        } catch (Exception e) {
			log.error("ERROR CLOSE[21pm]:",e);
		}
        log.debug("updateSorteoDiaria21: END");

    }

    @Scheduled(cron = "0 59 11 ? * SUN")
    public void updateSorteoChiquitica() {
    	log.debug("updateSorteoChiquitica: START");
    	log.info("updateSorteoChica: [CLOSE 12pm]");
        try {
        	if ( profileIdentifier.isProfileProdActive()) {
				Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 12);
    		}
		} catch (Exception e) {
			log.error("ERROR CLOSE[12pm]:",e);
		}
        log.debug("updateSorteoChiquitica: END");
    }
    
    @Scheduled(cron = "0 55 23 * * ?")
    public void updateSorteosMidnight() {
    	log.debug("updateSorteosMidnight: START");
    	log.info("updateSorteosMidnight: [CLOSE 11am, 12pm, 3pm, 9pm]");

    	if ( !profileIdentifier.isProfileProdActive()) {
			try {
	        	sorteoService.resetDayForUAT();
			} catch (Exception e) {
				log.error("ERROR [CLOSE 11am, 12pm, 3pm, 9pm]:",e);
			}
		}
		
		log.debug("updateSorteosMidnight: END");
    }

}
