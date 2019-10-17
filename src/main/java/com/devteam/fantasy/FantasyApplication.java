package com.devteam.fantasy;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.devteam.fantasy.math.MathUtil;
import com.devteam.fantasy.math.SorteoTotales;
import com.devteam.fantasy.repository.*;
import com.devteam.fantasy.util.*;
import com.devteam.fantasy.model.*;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.previousOrSame;


@SpringBootApplication
@EnableScheduling
public class FantasyApplication {

    @Autowired
    private SorteoRepository sorteoRepository;

    @Autowired
    private SorteoDiariaRepository sorteoDiariaRepository;
    
    @Autowired
    private SorteoTotales sorteoTotales;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    MonedaRepository monedaRepository;

    @Autowired
    TipoApostadorRepository tipoApostadorRepository;
    
    @Autowired
    ApuestaRepository apuestaRepository;

    @Autowired
    TipoChicaRepository tipoChicaRepository;

    @Autowired
    JugadorRepository jugadorRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    CambioRepository cambioRepository;

    @Autowired
    AsistenteRepository asistenteRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    SorteoTypeRepository sorteoTypeRepository;
    
    @Autowired
    JugadorSequenceRepository jugadorSequenceRepository;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Tegucigalpa"));   // It will set CST timezone
        System.out.println("Spring boot application running in UTC timezone :" + new Date());   // It will print UTC timezone
    }

    public static void main(String[] args) {
        SpringApplication.run(FantasyApplication.class, args);
    }

    /**
     * @param repo
     * @param estadoRepository
     * @param monedaRepository
     * @param tipoApostadorRepository
     * @param tipoChicaRepository
     * @return
     */
    /**
     * @param repo
     * @param estadoRepository
     * @param monedaRepository
     * @param tipoApostadorRepository
     * @param tipoChicaRepository
     * @return
     */
    @Bean
    public CommandLineRunner demoData(RoleRepository repo, EstadoRepository estadoRepository,
                                      MonedaRepository monedaRepository,
                                      TipoApostadorRepository tipoApostadorRepository,
                                      TipoChicaRepository tipoChicaRepository) {
        if (!repo.existsByName(RoleName.ROLE_ASIS) &&
                !repo.existsByName(RoleName.ROLE_ADMIN) &&
                !repo.existsByName(RoleName.ROLE_USER) &&
                !repo.existsByName(RoleName.ROLE_MASTER) &&
                !repo.existsByName(RoleName.ROLE_SUPERVISOR)) {
            repo.save(new Role(RoleName.ROLE_USER));
            repo.save(new Role(RoleName.ROLE_ADMIN));
            repo.save(new Role(RoleName.ROLE_ASIS));
            repo.save(new Role(RoleName.ROLE_MASTER));
            repo.save(new Role(RoleName.ROLE_SUPERVISOR));
        }

        if (!estadoRepository.existsByEstado(EstadoName.ABIERTA) &&
                !estadoRepository.existsByEstado(EstadoName.CERRADA) &&
                !estadoRepository.existsByEstado(EstadoName.BLOQUEADA)) {
            estadoRepository.save(new Estado(EstadoName.ABIERTA));
            estadoRepository.save(new Estado(EstadoName.CERRADA));
            estadoRepository.save(new Estado(EstadoName.BLOQUEADA));
        }

        if (!monedaRepository.existsByMonedaName(MonedaName.DOLAR) &&
                !monedaRepository.existsByMonedaName(MonedaName.LEMPIRA)) {
            monedaRepository.save(new Moneda(MonedaName.DOLAR));
            monedaRepository.save(new Moneda(MonedaName.LEMPIRA));
        }

        if (!tipoApostadorRepository.existsByApostadorName(ApostadorName.DIRECTO) &&
                !tipoApostadorRepository.existsByApostadorName(ApostadorName.MILES)) {
            tipoApostadorRepository.save(new TipoApostador(ApostadorName.DIRECTO));
            tipoApostadorRepository.save(new TipoApostador(ApostadorName.MILES));
        }

        if (!tipoChicaRepository.existsByChicaName(ChicaName.DIRECTO) &&
                !tipoChicaRepository.existsByChicaName(ChicaName.MILES) &&
                !tipoChicaRepository.existsByChicaName(ChicaName.PEDAZOS)) {
            tipoChicaRepository.save(new TipoChica(ChicaName.MILES));
            tipoChicaRepository.save(new TipoChica(ChicaName.DIRECTO));
            tipoChicaRepository.save(new TipoChica(ChicaName.PEDAZOS));
        }

        if (!statusRepository.existsByStatus(StatusName.CURRENT) &&
                !statusRepository.existsByStatus(StatusName.LAST) &&
                !statusRepository.existsByStatus(StatusName.OTHER)) {
            statusRepository.save(new Status(StatusName.CURRENT));
            statusRepository.save(new Status(StatusName.LAST));
            statusRepository.save(new Status(StatusName.OTHER));
        }

        if (!sorteoTypeRepository.existsBySorteoTypeName(SorteoTypeName.CHICA) &&
                !sorteoTypeRepository.existsBySorteoTypeName(SorteoTypeName.DIARIA)) {
            sorteoTypeRepository.save(new SorteoType(SorteoTypeName.CHICA));
            sorteoTypeRepository.save(new SorteoType(SorteoTypeName.DIARIA));
        }

        if (!userRepository.existsByUsername("C01") && !userRepository.existsByUsername("C02") ) {
        	createMaster("C01","Master1");
        	createAdmin("C02","Admin 2");
        	createAdmin("C03","Admin 3");
        	createAdmin("C04","Admin 4");
        	createSupervisor("C05","Supervisor 1");
        }

        if (!userRepository.existsByUsername("P001")
                && !userRepository.existsByUsername("P002")
                && !userRepository.existsByUsername("P003")
                && !userRepository.existsByUsername("P004")
                && !userRepository.existsByUsername("P005")
                && !userRepository.existsByUsername("P006")
        ) {
            Moneda dolar = monedaRepository.findByMonedaName(MonedaName.DOLAR);
            Moneda lempiras = monedaRepository.findByMonedaName(MonedaName.LEMPIRA);

            TipoApostador apuestaDirecta = tipoApostadorRepository.findByApostadorName(ApostadorName.DIRECTO);
            TipoApostador apuestaMiles = tipoApostadorRepository.findByApostadorName(ApostadorName.MILES);
            TipoChica chicaDirecta = tipoChicaRepository.findByChicaName(ChicaName.DIRECTO);
            TipoChica chicaMiles = tipoChicaRepository.findByChicaName(ChicaName.MILES);
            TipoChica chicaPedazos = tipoChicaRepository.findByChicaName(ChicaName.PEDAZOS);

//            Jugador p1 = createJugadorP("Usuario 1","P001",lempiras, apuestaMiles,chicaMiles, new double[] {12,1000}, new double[]{11,1000} );
//            jugadorSequenceRepository.getNextValue();
//            Jugador p2 = createJugadorP("Usuario 2","P002",lempiras, apuestaMiles,chicaDirecta, new double[] {12,1000}, new double[]{20,66} );
//            jugadorSequenceRepository.getNextValue();
//            Jugador p3 = createJugadorP("Usuario 3","P003",lempiras, apuestaMiles,chicaPedazos, new double[] {10,1000}, new double[]{22,0.30,20} );
//            jugadorSequenceRepository.getNextValue();
//            Jugador p4 = createJugadorP("Usuario 4","P004",lempiras, apuestaDirecta,chicaMiles, new double[] {18,66}, new double[]{15,1000} );
//            jugadorSequenceRepository.getNextValue();
//            Jugador p5 = createJugadorP("Usuario 5","P005",lempiras, apuestaDirecta,chicaDirecta, new double[] {20,66}, new double[]{22.5,60} );
//            jugadorSequenceRepository.getNextValue();
//            Jugador p6 = createJugadorP("Usuario 6","P006",lempiras, apuestaDirecta,chicaPedazos, new double[] {21,65}, new double[]{20,0.30,68} );
//            jugadorSequenceRepository.getNextValue();
//            Jugador p7 = createJugadorP("Usuario 7","P007",dolar, apuestaDirecta,chicaDirecta, new double[] {10,100}, new double[]{10,100} );
//            jugadorSequenceRepository.getNextValue();
//            
//            Asistente x1 = createJugadorX("Asistente 1", "P001x1", p1);
//            Asistente x2 = createJugadorX("Asistente 2", "P002x1", p2);
//            Asistente x3 = createJugadorX("Asistente 3", "P003x1", p3);
//            Asistente x4 = createJugadorX("Asistente 4", "P004x1", p4);
//            Asistente x5 = createJugadorX("Asistente 5", "P005x1", p5);
       
            Cambio cambio = new Cambio();
            if (cambioRepository.count() == 0) {
                cambio.setCambio(24.5);
                cambio.setCambioTime(new Timestamp(
                        ZonedDateTime.now().toInstant().toEpochMilli()
                ));
                cambio = cambioRepository.save(cambio);
            }

            Date date = new Date();
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int year = localDate.getYear();
            int month = localDate.getMonthValue();
            int day = localDate.getDayOfMonth();


            Timestamp timestamp = (new Timestamp(
                    ZonedDateTime.of(year, month, day, 11, 0, 0, 0,
                            ZoneId.of("America/Tegucigalpa")
                    ).toInstant().toEpochMilli()
            ));

            SorteoType diariaSorteoType = sorteoTypeRepository.getBySorteoTypeName(SorteoTypeName.DIARIA);
            SorteoType chicaSorteoType = sorteoTypeRepository.getBySorteoTypeName(SorteoTypeName.CHICA);
            Sorteo sorteoDiaria11 = null;
            Sorteo sorteoDiaria15 = null;
            Sorteo sorteoDiaria21 = null;
            Sorteo sorteoChica = null;
            if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            	sorteoDiaria11 = newSorteo(estadoRepository, timestamp, diariaSorteoType);
            }


            timestamp = (new Timestamp(
                    ZonedDateTime.of(year, month, day, 15, 0, 0, 0,
                            ZoneId.of("America/Tegucigalpa")
                    ).toInstant().toEpochMilli()
            ));

            if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            	sorteoDiaria15 = newSorteo(estadoRepository, timestamp, diariaSorteoType);
            }

            timestamp = (new Timestamp(
                    ZonedDateTime.of(year, month, day, 21, 0, 0, 0,
                            ZoneId.of("America/Tegucigalpa")
                    ).toInstant().toEpochMilli()
            ));

            if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            	sorteoDiaria21 = newSorteo(estadoRepository, timestamp, diariaSorteoType);
            }


            LocalDate monday = localDate.with(previousOrSame(MONDAY));
            LocalDate nextSunday =LocalDate.now().with( SUNDAY );

            timestamp = Timestamp.valueOf(LocalDateTime.of(nextSunday, LocalTime.NOON));
            if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            	sorteoChica = newSorteo(estadoRepository, timestamp, chicaSorteoType);
            }
            
            sorteoTotales.setMonedaName(MonedaName.LEMPIRA);
            /**
             * Jugador 1 
             */
//            insertApuestaDiaria(sorteoDiaria11.getId(), p1, cambio, 0, 25);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p7, cambio, 0, 25);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p1, cambio, 0, 25);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p7, cambio, 0, 25);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p1, cambio, 3, 30);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p1, cambio, 10, 20);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p1, cambio, 11, 46);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p1, cambio, 15, 28);
////            insertApuestaDiaria(sorteoDiaria11.getId(), x1, cambio, 0, 33);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x1, cambio, 5, 30);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x1, cambio, 8, 50);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x1, cambio, 10, 18);
//            
//            insertApuestaDiaria(sorteoDiaria15.getId(), p1, cambio, 0, 25);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p1, cambio, 3, 30);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p1, cambio, 10, 20);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p1, cambio, 11, 46);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p1, cambio, 15, 28);
////            insertApuestaDiaria(sorteoDiaria11.getId(), x1, cambio, 0, 33);
//            insertApuestaDiaria(sorteoDiaria15.getId(), x1, cambio, 5, 30);
//            insertApuestaDiaria(sorteoDiaria15.getId(), x1, cambio, 8, 50);
//            insertApuestaDiaria(sorteoDiaria15.getId(), x1, cambio, 10, 18);
//            
//            insertApuestaDiaria(sorteoDiaria21.getId(), p1, cambio, 0, 25);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p1, cambio, 3, 30);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p1, cambio, 10, 20);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p1, cambio, 11, 46);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p1, cambio, 15, 28);
////            insertApuestaDiaria(sorteoDiaria11.getId(), x1, cambio, 0, 33);
//            insertApuestaDiaria(sorteoDiaria21.getId(), x1, cambio, 5, 30);
//            insertApuestaDiaria(sorteoDiaria21.getId(), x1, cambio, 8, 50);
//            insertApuestaDiaria(sorteoDiaria21.getId(), x1, cambio, 10, 18);
//            
//            insertApuestaChica(sorteoChica.getId(),p1, cambio,0,45);
//            insertApuestaChica(sorteoChica.getId(),p1, cambio,3,35);
//            insertApuestaChica(sorteoChica.getId(),p1, cambio,10,26);
//            insertApuestaChica(sorteoChica.getId(),p1, cambio,11,20);
//            insertApuestaChica(sorteoChica.getId(),p1, cambio,15,30);
//            insertApuestaChica(sorteoChica.getId(),x1, cambio,5,40);
//            insertApuestaChica(sorteoChica.getId(),x1, cambio,8,36);
//            insertApuestaChica(sorteoChica.getId(),x1, cambio,10,55);
//            insertApuestaChica(sorteoChica.getId(),x1, cambio,18,1);
//            
//            /**
//             * Jugador 2
//             */
//            insertApuestaDiaria(sorteoDiaria11.getId(), p2, cambio, 0, 15);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p2, cambio, 3, 18);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p2, cambio, 10, 45);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p2, cambio, 11, 48);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p2, cambio, 17, 10);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x2, cambio, 5, 50);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x2, cambio, 8, 40);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x2, cambio, 10, 15);
//            
//            insertApuestaDiaria(sorteoDiaria15.getId(), p2, cambio, 0, 15);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p2, cambio, 3, 18);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p2, cambio, 10, 45);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p2, cambio, 11, 48);
//            insertApuestaDiaria(sorteoDiaria15.getId(), p2, cambio, 17, 10);
//            insertApuestaDiaria(sorteoDiaria15.getId(), x2, cambio, 5, 50);
//            insertApuestaDiaria(sorteoDiaria15.getId(), x2, cambio, 8, 40);
//            insertApuestaDiaria(sorteoDiaria15.getId(), x2, cambio, 10, 15);
//            
//            insertApuestaDiaria(sorteoDiaria21.getId(), p2, cambio, 0, 15);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p2, cambio, 3, 18);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p2, cambio, 10, 45);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p2, cambio, 11, 48);
//            insertApuestaDiaria(sorteoDiaria21.getId(), p2, cambio, 17, 10);
//            insertApuestaDiaria(sorteoDiaria21.getId(), x2, cambio, 5, 50);
//            insertApuestaDiaria(sorteoDiaria21.getId(), x2, cambio, 8, 40);
//            insertApuestaDiaria(sorteoDiaria21.getId(), x2, cambio, 10, 15);
//            
//            insertApuestaChica(sorteoChica.getId(),p2, cambio,0,26);
//            insertApuestaChica(sorteoChica.getId(),p2, cambio,3,58);
//            insertApuestaChica(sorteoChica.getId(),p2, cambio,10,45);
//            insertApuestaChica(sorteoChica.getId(),p2, cambio,11,44);
//            insertApuestaChica(sorteoChica.getId(),p2, cambio,15,88);
//            insertApuestaChica(sorteoChica.getId(),x2, cambio,5,42);
//            insertApuestaChica(sorteoChica.getId(),x2, cambio,8,105);
//            insertApuestaChica(sorteoChica.getId(),x2, cambio,10,48);
//            insertApuestaChica(sorteoChica.getId(),x2, cambio,19,70);
//            
//            /**
//             * Jugador 3
//             */
//            insertApuestaDiaria(sorteoDiaria11.getId(), p3, cambio, 0, 80);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p3, cambio, 3, 40);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p3, cambio, 10, 59);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p3, cambio, 11, 63);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p3, cambio, 19, 45);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x3, cambio, 5, 44);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x3, cambio, 8, 40);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x3, cambio, 10, 23);
//           
//            insertApuestaChica(sorteoChica.getId(),p3, cambio,0,160);
//            insertApuestaChica(sorteoChica.getId(),p3, cambio,3,180);
//            insertApuestaChica(sorteoChica.getId(),p3, cambio,10,240);
//            insertApuestaChica(sorteoChica.getId(),p3, cambio,11,340);
//            insertApuestaChica(sorteoChica.getId(),p3, cambio,15,170);
//            insertApuestaChica(sorteoChica.getId(),x3, cambio,5,180);
//            insertApuestaChica(sorteoChica.getId(),x3, cambio,8,160);
//            insertApuestaChica(sorteoChica.getId(),x3, cambio,10,190);
//            insertApuestaChica(sorteoChica.getId(),x3, cambio,14,190);
//            
//            /**
//             * Jugador 4
//             */
//            insertApuestaDiaria(sorteoDiaria11.getId(), p4, cambio, 0, 1200);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p4, cambio, 3, 1000);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p4, cambio, 10, 800);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p4, cambio, 11, 450);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p4, cambio, 17, 20);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x4, cambio, 5, 1000);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x4, cambio, 8, 1450);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x4, cambio, 10, 1800);
//            
//            insertApuestaChica(sorteoChica.getId(),p4, cambio,0,20);
//            insertApuestaChica(sorteoChica.getId(),p4, cambio,3,20);
//            insertApuestaChica(sorteoChica.getId(),p4, cambio,10,20);
//            insertApuestaChica(sorteoChica.getId(),p4, cambio,11,20);
//            insertApuestaChica(sorteoChica.getId(),p4, cambio,15,20);
//            insertApuestaChica(sorteoChica.getId(),x4, cambio,5,20);
//            insertApuestaChica(sorteoChica.getId(),x4, cambio,8,20);
//            insertApuestaChica(sorteoChica.getId(),x4, cambio,10,20);
//            insertApuestaChica(sorteoChica.getId(),x4, cambio,12,20);
//            
//            /**
//             * Jugador 5
//             */
//            insertApuestaDiaria(sorteoDiaria11.getId(), p5, cambio, 0, 120);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p5, cambio, 3, 100);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p5, cambio, 10, 145);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p5, cambio, 11, 160);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p5, cambio, 17, 5);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x5, cambio, 5, 120);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x5, cambio, 8, 90);
//            insertApuestaDiaria(sorteoDiaria11.getId(), x5, cambio, 10, 49);
//            
//            insertApuestaChica(sorteoChica.getId(),p5, cambio,0,156);
//            insertApuestaChica(sorteoChica.getId(),p5, cambio,3,147);
//            insertApuestaChica(sorteoChica.getId(),p5, cambio,10,110);
//            insertApuestaChica(sorteoChica.getId(),p5, cambio,11,210);
//            insertApuestaChica(sorteoChica.getId(),p5, cambio,15,41);
//            insertApuestaChica(sorteoChica.getId(),x5, cambio,5,100);
//            insertApuestaChica(sorteoChica.getId(),x5, cambio,8,250);
//            insertApuestaChica(sorteoChica.getId(),x5, cambio,10,200);
//            insertApuestaChica(sorteoChica.getId(),x5, cambio,18,40);
//
//            /**
//             * Jugador 6
//             */
//            
//            insertApuestaDiaria(sorteoDiaria11.getId(), p6, cambio, 0, 210);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p6, cambio, 3, 250);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p6, cambio, 10, 180);
//            insertApuestaDiaria(sorteoDiaria11.getId(), p6, cambio, 11, 350);
//            
//            insertApuestaChica(sorteoChica.getId(),p6, cambio,0,50);
//            insertApuestaChica(sorteoChica.getId(),p6, cambio,3,120);
//            insertApuestaChica(sorteoChica.getId(),p6, cambio,10,240);
//            insertApuestaChica(sorteoChica.getId(),p6, cambio,11,45);
//            insertApuestaChica(sorteoChica.getId(),p6, cambio,15,160);
//            insertApuestaChica(sorteoChica.getId(),p6, cambio,18,10);


            List<SorteoDiaria> sorteoDiarias = sorteoDiariaRepository
                    .findAllBySorteoTimeLessThan(new Timestamp(System.currentTimeMillis()));

            sorteoDiarias.forEach(sorteoDiaria -> {
                Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
                sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.ABIERTA));
                sorteoRepository.save(sorteo);
            });

        }
        return null;
    }
    
    private void createMaster(String username, String name) {
    	User user = new User(name, username, encoder.encode("123456789"));
        Set<Role> roles = new HashSet<>();
        Role masterRole = roleRepository.findByName(RoleName.ROLE_MASTER)
                	.orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
        Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
            	.orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
        
        roles.add(masterRole);
        roles.add(adminRole);
        
        user.setRoles(roles);
        user.setUserState(UserState.ACTIVE);
        userRepository.save(user);
    }
    
    private void createAdmin(String username, String name) {
    	User user = new User(name, username, encoder.encode("123456789"));
        Set<Role> roles = new HashSet<>();
        Role masterRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                	.orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
        roles.add(masterRole);
        user.setRoles(roles);
        user.setUserState(UserState.ACTIVE);
        userRepository.save(user);
    }
    
    private void createSupervisor(String username, String name) {
    	User user = new User(name, username, encoder.encode("123456789"));
        Set<Role> roles = new HashSet<>();
        Role masterRole = roleRepository.findByName(RoleName.ROLE_SUPERVISOR)
                	.orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
        
        roles.add(masterRole);
        
        user.setRoles(roles);
        user.setUserState(UserState.ACTIVE);
        userRepository.save(user);
    }
    
    private Jugador createJugadorP(String name, String username, Moneda dolar,TipoApostador tipoApostador, TipoChica tipoChica,double valoresDiarios[], double valoresChica[]) {
    	Jugador jugador = new Jugador(name,username,encoder.encode("123456789"), dolar, tipoApostador, tipoChica);
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
        							.orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
        roles.add(userRole);
        jugador.setRoles(roles);
        
        if (tipoApostador.getApostadorName().equals(ApostadorName.MILES)) {
        	jugador.setCostoMil(valoresDiarios[0]);
            jugador.setPremioMil(valoresDiarios[1]);
        }else if (tipoApostador.getApostadorName().equals(ApostadorName.DIRECTO)) {
        	jugador.setComisionDirecto(valoresDiarios[0]);
        	jugador.setPremioDirecto(valoresDiarios[1]);
        }
        
        if(tipoChica.getChicaName().equals(ChicaName.MILES)) {
            jugador.setCostoChicaMiles(valoresChica[0]);
            jugador.setPremioChicaMiles(valoresChica[1]);
        }else if(tipoChica.getChicaName().equals(ChicaName.DIRECTO)) {
        	jugador.setComisionChicaDirecto(valoresChica[0]);
        	jugador.setPremioChicaDirecto(valoresChica[1]);
        }else if(tipoChica.getChicaName().equals(ChicaName.PEDAZOS)) {
        	jugador.setComisionChicaPedazos(valoresChica[0]);
        	jugador.setCostoChicaPedazos(valoresChica[1]);
        	jugador.setPremioChicaPedazos(valoresChica[2]);
        }
        
        jugador.setUserState(UserState.ACTIVE);
        return jugadorRepository.save(jugador);
    }
    
    private Asistente createJugadorX(String name, String username, Jugador jugadorP) {
    	Set<Role> roles = new HashSet<>();
        Role role = roleRepository.findByName(RoleName.ROLE_ASIS)
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
        
        roles.add(role);
        Asistente asistente = new Asistente(name, username, encoder.encode("123456789"));
        asistente.setRoles(roles);
        asistente.setUserState(UserState.ACTIVE);
        asistenteRepository.save(asistente);
        
        asistente.setJugador(jugadorP);
        return asistenteRepository.save(asistente);
    }

    private Sorteo newSorteo(EstadoRepository estadoRepository, Timestamp timestamp, SorteoType sorteoType) {
    	Sorteo sorteo1 = null;
    	if (!sorteoRepository.existsSorteoBySorteoTime(timestamp)) {
    		sorteo1 = new Sorteo();
    		sorteo1.setEstado(estadoRepository.getEstadoByEstado(EstadoName.ABIERTA));
            sorteo1.setSorteoTime(timestamp);
            sorteo1.setStatus(statusRepository.getByStatus(StatusName.CURRENT));
            sorteo1.setSorteoType(sorteoType);
            sorteoRepository.save(sorteo1);
            SorteoDiaria sorteoDiaria1 = new SorteoDiaria();
            sorteoDiaria1.setId(sorteo1.getId());
            sorteoDiaria1.setSorteo(sorteo1);
            sorteoDiaria1.setSorteoTime(timestamp);
            sorteoDiariaRepository.save(sorteoDiaria1);
        }
    	return sorteo1;
    }

    private void insertApuestaDiaria(Long sorteoid,User user, Cambio cambio, int numero, int cantidad) {
        double comision = 0d;
    	
        Jugador jugador = null;
	    if (user instanceof Jugador) {
	    	jugador = (Jugador) user;
	    } else if (user instanceof Asistente) {
	    	jugador = ((Asistente) user).getJugador();
	    }
	    comision = MathUtil.getComisionRate(jugador, SorteoTypeName.DIARIA).doubleValue(); 
        
	    
	    
    	Apuesta apuesta = new Apuesta();
        Optional<SorteoDiaria> sorteoDiaria = sorteoDiariaRepository.findById(sorteoid);
        sorteoDiaria.ifPresent(apuesta::setSorteoDiaria);
        apuesta.setNumero(numero);
        apuesta.setCantidad(Double.valueOf(cantidad));
        
        apuesta.setCambio(cambio);
        apuesta.setUser(user);
        apuesta.setDate(Timestamp.valueOf(LocalDateTime.now()));
        double costo = MathUtil.getCantidadMultiplier(jugador, apuesta, SorteoTypeName.DIARIA, MonedaName.LEMPIRA).doubleValue();
        apuestaRepository.save(apuesta);
    }
    
    private void insertApuestaChica(Long sorteoid,User user, Cambio cambio, int numero, int cantidad) {
        double comision = 0d;
        double costoPedazo = 0d;
    	
        Jugador jugador = null;
	    if (user instanceof Jugador) {
	    	jugador = (Jugador) user;
	    } else if (user instanceof Asistente) {
	    	jugador = ((Asistente) user).getJugador();
	    }
	    
	    comision = MathUtil.getComisionRate(jugador, SorteoTypeName.CHICA).doubleValue(); 
	    costoPedazo = jugador.getCostoChicaPedazos()==0?1:jugador.getCostoChicaPedazos();
	    
    	Apuesta apuesta = new Apuesta();
        Optional<SorteoDiaria> sorteoDiaria = sorteoDiariaRepository.findById(sorteoid);
        sorteoDiaria.ifPresent(apuesta::setSorteoDiaria);
        apuesta.setNumero(numero);
        apuesta.setCantidad(Double.valueOf(cantidad));
        apuesta.setCambio(cambio);
        apuesta.setUser(user);
        apuesta.setDate(Timestamp.valueOf(LocalDateTime.now()));
        
        double costo = MathUtil.getCantidadMultiplier(jugador, apuesta, SorteoTypeName.CHICA, MonedaName.LEMPIRA).doubleValue();
        apuestaRepository.save(apuesta);
    }
    
    
    @Bean
    InitializingBean sendDatabase() {
        return () -> {


        };
    }


}
