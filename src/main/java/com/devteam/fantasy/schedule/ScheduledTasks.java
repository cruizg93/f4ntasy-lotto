package com.devteam.fantasy.schedule;


import com.devteam.fantasy.repository.*;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

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
    private SorteoTypeRepository sorteoTypeRepository;

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

//    @Scheduled(fixedRate = 5000)
//    public void reportCurrentTime() {
//        log.info("The time is now {}", dateFormat.format(new Date()));
//    }

    //@Scheduled(cron = "[Seconds] [Minutes] [Hours] [Day of month] [Month] [Day of week] [Year]")
    /*
     * Below is a breakdown of the components that build a cron expression.
     *
     *     Seconds can have values 0-59 or the special characters , - * / .
     *     Minutes can have values 0-59 or the special characters , - * / .
     *     Hours can have values 0-59 or the special characters , - * / .
     *     Day of month can have values 1-31 or the special characters , - * ? / L W C .
     *     Month can have values 1-12, JAN-DEC or the special characters , - * / .
     *     Day of week can have values 1-7, SUN-SAT or the special characters , - * ? / L C # .
     *     Year can be empty, have values 1970-2099 or the special characters , - * / .
     *
     * */
    /*@Scheduled(cron = "0 1 0 * * ?")
    public void createSorteoDiaria() {
    	log.debug("createSorteoDiaria: START");
//        Util.deleteSorteosDia(sorteoRepository, sorteoDiariaRepository, apuestaRepository, historicoApuestaRepository);
//        Util.insertSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, sorteoTypeRepository, SorteoTypeName.DIARIA, 11);
//        Util.insertSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, sorteoTypeRepository, SorteoTypeName.DIARIA, 15);
//        Util.insertSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, sorteoTypeRepository, SorteoTypeName.DIARIA, 21);
        log.debug("createSorteoDiaria: END");
    }*/

//    @Scheduled(cron = "0 1 0 * * MON")
//    public void updateJugadorBalance() {
//    	log.debug("updateJugadorBalance: START");
//        Util.updateJugadoBalance(jugadorRepository);
//        log.debug("updateJugadorBalance: END");
//    }

//    @Scheduled(cron = "0 1 0 * * MON")
//    public void updateSorteoStatus() {
//        Util.updateSorteoStatus(sorteoRepository, statusRepository, sorteoDiariaRepository);
//    }
//    @Scheduled(cron = "0 5 0 ? * MON")
//    public void createSorteoChiquitica() {
//        Util.insertSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, sorteoTypeRepository, SorteoTypeName.CHICA, 1);
//    }

    @Scheduled(cron = "0 55 10 * * ?")
    public void updateSorteoDiaria11() {
    	log.debug("updateSorteoDiaria11: START");
    	log.info("updateSorteoDiaria11: [CLOSE 11am]");
        try {
			Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 11);
		} catch (Exception e) {
			log.error("ERROR CLOSE[11am]:",e);
		}
        log.debug("updateSorteoDiaria11: END");
    }

    @Scheduled(cron = "0 55 14 * * ?")
    public void updateSorteoDiaria15() {
    	log.debug("updateSorteoDiaria15: START");
    	log.info("updateSorteoDiaria15: [CLOSE 15pm]");
        try {
			Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 15);
        } catch (Exception e) {
			log.error("ERROR CLOSE[15pm]:",e);
		}
        log.debug("updateSorteoDiaria15: END");

    }

    @Scheduled(cron = "0 55 20 * * ?")
    public void updateSorteoDiaria21() {
    	log.debug("updateSorteoDiaria21: START");
    	log.info("updateSorteoDiaria21: [CLOSE 21pm]");
        try {
			Util.updateSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, 21);
        } catch (Exception e) {
			log.error("ERROR CLOSE[21pm]:",e);
		}
        log.debug("updateSorteoDiaria21: END");

    }

    @Scheduled(cron = "0 55 11 ? * SUN")
    public void updateSorteoChiquitica() {
    	log.debug("updateSorteoChiquitica: START");
    	log.info("updateSorteoChica: [CLOSE 12pm]");
        try {
			Util.updateChicaSorteo(estadoRepository, sorteoRepository, sorteoTypeRepository);
		} catch (Exception e) {
			log.error("ERROR CLOSE[12pm]:",e);
		}
        log.debug("updateSorteoChiquitica: END");
    }

}
