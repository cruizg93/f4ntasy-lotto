package com.devteam.fantasy;

import com.devteam.fantasy.model.*;
import com.devteam.fantasy.repository.*;
import com.devteam.fantasy.util.*;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.previousOrSame;


@SpringBootApplication
@EnableScheduling
public class FantasyApplication {

    @Autowired
    private SorteoRepository sorteoRepository;

    @Autowired
    private SorteoDiariaRepository sorteoDiariaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    MonedaRepository monedaRepository;

    @Autowired
    TipoApostadorRepository tipoApostadorRepository;

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
    PlayerCountRepository playerCountRepository;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Tegucigalpa"));   // It will set CST timezone
        System.out.println("Spring boot application running in UTC timezone :" + new Date());   // It will print UTC timezone
    }

    public static void main(String[] args) {
        SpringApplication.run(FantasyApplication.class, args);
    }

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
                !monedaRepository.existsByMonedaName(MonedaName.LEMPIRAS)) {
            monedaRepository.save(new Moneda(MonedaName.DOLAR));
            monedaRepository.save(new Moneda(MonedaName.LEMPIRAS));
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

        if (!userRepository.existsByUsername("C01") &&
                !userRepository.existsByUsername("C02")

        ) {
            User user = new User("C01", "C01", encoder.encode("123456789"));
            Set<Role> roles = new HashSet<>();
            Role masterRole = roleRepository.findByName(RoleName.ROLE_MASTER)
                    .orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
            roles.add(masterRole);
            user.setRoles(roles);
            userRepository.save(user);
            User user1 = new User("C02", "C02", encoder.encode("123456789"));
            Set<Role> roles1 = new HashSet<>();
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
            roles1.add(masterRole);
            roles1.add(adminRole);
            user1.setRoles(roles1);
            userRepository.save(user1);
        }

        if (!userRepository.existsByUsername("JP01$")
                && !userRepository.existsByUsername("JP02$")
                && !userRepository.existsByUsername("JP03L")
                && !userRepository.existsByUsername("JP04L")
                && !userRepository.existsByUsername("P001")
                && !userRepository.existsByUsername("P001x1")
        ) {
            Moneda dolar = monedaRepository.findByMonedaName(MonedaName.DOLAR);
            Moneda lempiras = monedaRepository.findByMonedaName(MonedaName.LEMPIRAS);

            TipoApostador tipoApostador = tipoApostadorRepository.findByApostadorName(ApostadorName.DIRECTO);
            TipoChica tipoChica = tipoChicaRepository.findByChicaName(ChicaName.DIRECTO);

            TipoApostador tipoApostador1 = tipoApostadorRepository.findByApostadorName(ApostadorName.MILES);
            TipoChica tipoChica1 = tipoChicaRepository.findByChicaName(ChicaName.PEDAZOS);

//        jugadorRepository.deleteAll();
            Jugador jDolar1 = new Jugador("Jugador 1",
                    "JP01$",
                    encoder.encode("123456789"), dolar, tipoApostador, tipoChica);
//        User user3 = new User("Jugador1", "JP01$", encoder.encode("123456789"));
            Set<Role> roles2 = new HashSet<>();
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
            roles2.add(userRole);
            jDolar1.setRoles(roles2);
            jugadorRepository.save(jDolar1);


            Jugador jDolar2 = new Jugador("Jugador 2",
                    "JP02$",
                    encoder.encode("123456789"), dolar, tipoApostador, tipoChica);
            roles2.add(userRole);
            jDolar1.setRoles(roles2);
            jugadorRepository.save(jDolar2);

            Jugador jLempiras = new Jugador("Jugador 3",
                    "JP03L",
                    encoder.encode("123456789"), lempiras, tipoApostador1, tipoChica1);
            jLempiras.setRoles(roles2);
            jugadorRepository.save(jLempiras);

            Jugador jLempiras1 = new Jugador("Jugador 4",
                    "JP04L",
                    encoder.encode("123456789"), lempiras, tipoApostador1, tipoChica1);
            jLempiras.setRoles(roles2);
            jugadorRepository.save(jLempiras1);

            Jugador jP1 = new Jugador("Jugador 5",
                    "P001",
                    encoder.encode("123456789"), lempiras, tipoApostador1, tipoChica1);
            jP1.setCostoMil(12);
            jP1.setPremioMil(10);
            jP1.setComisionChicaPedazos(20);
            jP1.setCostoChicaPedazos(0.3);
            jP1.setPremioChicaPedazos(20);
            jP1.setRoles(roles2);

            PlayerCount playerCount = new PlayerCount();
            playerCount.setCount(1L);
            playerCountRepository.save(playerCount);

            jugadorRepository.save(jP1);

            Set<Role> roles3 = new HashSet<>();
            Role asiRole = roleRepository.findByName(RoleName.ROLE_ASIS)
                    .orElseThrow(() -> new RuntimeException("Fail! -> Cause: Master Role not find."));
            roles3.add(asiRole);
            Asistente asistente = new Asistente("Jugador 9",
                    "P001x1",
                    encoder.encode("123456789"));
            asistente.setRoles(roles3);
            asistenteRepository.save(asistente);
            asistente.setJugador(jP1);
            asistenteRepository.save(asistente);
        }


        if (cambioRepository.count() == 0) {
            Cambio cambio = new Cambio();
            cambio.setCambio(24.5);
            cambio.setCambioTime(new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            cambioRepository.save(cambio);
        }

        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

//        userRepository.save(user3);

        Timestamp timestamp = (new Timestamp(
                ZonedDateTime.of(year, month, day, 11, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));

        SorteoType diariaSorteoType = sorteoTypeRepository.getBySorteoTypeName(SorteoTypeName.DIARIA);
        SorteoType chicaSorteoType = sorteoTypeRepository.getBySorteoTypeName(SorteoTypeName.CHICA);
        if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            newSorteo(estadoRepository, timestamp, diariaSorteoType);
        }


        timestamp = (new Timestamp(
                ZonedDateTime.of(year, month, day, 15, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));

        if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            newSorteo(estadoRepository, timestamp, diariaSorteoType);
        }

        timestamp = (new Timestamp(
                ZonedDateTime.of(year, month, day, 21, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));

        if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            newSorteo(estadoRepository, timestamp, diariaSorteoType);
        }


        LocalDate monday = localDate.with(previousOrSame(MONDAY));
        LocalDate nextSunday =LocalDate.now().with( next( SUNDAY ) );
//        timestamp = (new Timestamp(
//                ZonedDateTime.of(monday.getYear(), monday.getMonthValue(), monday.getDayOfMonth(), 0, 1, 0, 0,
//                        ZoneId.of("America/Tegucigalpa")
//                ).toInstant().toEpochMilli()
//        ));
        timestamp = Timestamp.valueOf(LocalDateTime.of(nextSunday, LocalTime.MIDNIGHT));
        if (!sorteoDiariaRepository.existsSorteoActivoBySorteoTime(timestamp)) {
            newSorteo(estadoRepository, timestamp, chicaSorteoType);
        }

        List<SorteoDiaria> sorteoDiarias = sorteoDiariaRepository
                .findAllBySorteoTimeLessThan(new Timestamp(System.currentTimeMillis()));

        sorteoDiarias.forEach(sorteoDiaria -> {
            Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
            if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
                sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
                sorteoRepository.save(sorteo);
            }

        });
        return null;
    }

    private void newSorteo(EstadoRepository estadoRepository, Timestamp timestamp, SorteoType sorteoType) {
        if (!sorteoRepository.existsSorteoBySorteoTime(timestamp)) {
            Sorteo sorteo1 = new Sorteo();
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
    }

    @Bean
    InitializingBean sendDatabase() {
        return () -> {


        };
    }


}
