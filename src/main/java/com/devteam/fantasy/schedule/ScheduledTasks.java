package com.devteam.fantasy.schedule;


import com.devteam.fantasy.repository.*;
import com.devteam.fantasy.service.SorteoService;
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
    Environment env;
    
    @Autowired 
    private SorteoService sorteoService;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);


    @Scheduled(cron = "0 59 10 * * ?")
    public void updateSorteoDiaria11() {
    	log.debug("updateSorteoDiaria11: START");
    	log.info("updateSorteoDiaria11: [CLOSE 11am]");
        try {
        	String[] activeProfiles = env.getActiveProfiles();
    		if ( activeProfiles != null && activeProfiles.length>0) {
    			if( Arrays.asList(activeProfiles).contains("prod")) {
    				
    				Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 11);
    				
    			}
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
        	String[] activeProfiles = env.getActiveProfiles();
    		if ( activeProfiles != null && activeProfiles.length>0) {
    			if( Arrays.asList(activeProfiles).contains("prod")) {
    				
    				Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 15);
    				
    			}
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
        	String[] activeProfiles = env.getActiveProfiles();
    		if ( activeProfiles != null && activeProfiles.length>0) {
    			if( Arrays.asList(activeProfiles).contains("prod")) {
    				
    				Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 21);
    				
    			}
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
        	String[] activeProfiles = env.getActiveProfiles();
    		if ( activeProfiles != null && activeProfiles.length>0) {
    			if( Arrays.asList(activeProfiles).contains("prod")) {
    				
    				Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 12);
    				
    			}
    		}
		} catch (Exception e) {
			log.error("ERROR CLOSE[12pm]:",e);
		}
        log.debug("updateSorteoChiquitica: END");
    }
    
    @Scheduled(cron = "0 5 0 * * ?")
    public void updateSorteosMidnight() {
    	log.debug("updateSorteosMidnight: START");
    	log.info("updateSorteosMidnight: [CLOSE 11am, 12pm, 3pm, 9pm]");
    	
    	String[] activeProfiles = env.getActiveProfiles();
		if ( activeProfiles != null && activeProfiles.length>0) {
			if( !Arrays.asList(activeProfiles).contains("prod")) {
				try {
		        	sorteoService.resetDayForUAT();
				} catch (Exception e) {
					log.error("ERROR [CLOSE 11am, 12pm, 3pm, 9pm]:",e);
				}
			}
		}
		
		log.debug("updateSorteosMidnight: END");
    }

}
