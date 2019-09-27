package com.devteam.fantasy.controller;


import com.devteam.fantasy.math.MathUtil;
import com.devteam.fantasy.message.request.*;
import com.devteam.fantasy.message.response.*;
import com.devteam.fantasy.model.*;
import com.devteam.fantasy.repository.*;
import com.devteam.fantasy.security.jwt.JwtProvider;
import com.devteam.fantasy.service.AdminService;
import com.devteam.fantasy.service.HistoryService;
import com.devteam.fantasy.service.HistoryServiceImpl;
import com.devteam.fantasy.service.SorteoService;
import com.devteam.fantasy.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import javax.validation.Valid;
import java.sql.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    JugadorRepository jugadorRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    TipoApostadorRepository tipoApostadorRepository;

    @Autowired
    TipoChicaRepository tipoChicaRepository;

    @Autowired
    MonedaRepository monedaRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    CambioRepository cambioRepository;

    @Autowired
    RestriccionRepository restriccionRepository;

    @Autowired
    AsistenteRepository asistenteRepository;

    @Autowired
    SorteoRepository sorteoRepository;

    @Autowired
    StatusRepository statusRepository;

    @Autowired
    SorteoDiariaRepository sorteoDiariaRepository;

    @Autowired
    ResultadoRepository resultadoRepository;

    @Autowired
    ApuestaRepository apuestaRepository;

    @Autowired
    NumeroGanadorRepository numeroGanadorRepository;

    @Autowired
    EstadoRepository estadoRepository;

    @Autowired
    HistoricoApuestaRepository historicoApuestaRepository;

    @Autowired
    PlayerCountRepository playerCountRepository;

    @Autowired
    private SorteoTypeRepository sorteoTypeRepository;

    @Autowired
    private SorteoService sorteoService;
    
    @Autowired
    private AdminService adminService;
    
    
    @PostMapping("/bono/jugadores/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> submitBono(@PathVariable Long id, @Valid @RequestBody BonoRequest request){
    	try {
    		adminService.submitBono(request, id);
    	}catch (Exception e) {
    		return new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	return new ResponseEntity<String>("success",HttpStatus.OK);
    }
    
    
    @GetMapping("/jugadores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<JugadorSingleResponse> findAllJugadores() {
        List<JugadorSingleResponse> list=new ArrayList<>();
        list.add(new JugadorSingleResponse());
        list.addAll(jugadorRepository.findAll().stream()
                .map(jugador -> {
                    JugadorSingleResponse obj = new JugadorSingleResponse();
                    obj.setId(jugador.getId());
                    String moneda = jugador.getMoneda().getMonedaName().equals(MonedaName.LEMPIRA) ? " L " : " $ ";
                    obj.setUsername(jugador.getUsername()+ " - " + moneda +" "+jugador.getName() );
                    return obj;
                }).collect(Collectors.toList()));
        return list;
    }

    @GetMapping("/jugador/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public JugadorDataResponse getJugadorById(@PathVariable Long id) {
        JugadorDataResponse jugadorDataResponse = new JugadorDataResponse();
        Jugador jugador = jugadorRepository.getById(id);

        jugadorDataResponse.setPremioChicaPedazos(jugador.getPremioChicaPedazos());
        jugadorDataResponse.setPremioMil(jugador.getPremioMil());
        jugadorDataResponse.setPremioChicaMiles(jugador.getPremioChicaMiles());
        jugadorDataResponse.setPremioChicaDirecto(jugador.getPremioChicaDirecto());
        jugadorDataResponse.setPremioDirecto(jugador.getPremioDirecto());

        jugadorDataResponse.setComisionChicaPedazos(jugador.getComisionChicaPedazos());
        jugadorDataResponse.setComisionDirecto(jugador.getComisionDirecto());
        jugadorDataResponse.setComisionChicaDirecto(jugador.getComisionChicaDirecto());

        jugadorDataResponse.setCostoMil(jugador.getCostoMil());
        jugadorDataResponse.setCostoChicaMiles(jugador.getCostoChicaMiles());
        jugadorDataResponse.setCostoChicaPedazos(jugador.getCostoChicaPedazos());

        jugadorDataResponse.setBalance(jugador.getBalance());
        jugadorDataResponse.setId(jugador.getId());
        jugadorDataResponse.setMoneda(jugador.getMoneda());

        jugadorDataResponse.setUsername(jugador.getUsername());
        jugadorDataResponse.setName(jugador.getName());
        jugadorDataResponse.setEditable(true);
        if(!apuestaRepository.findAllByUser(userRepository.getById(id)).isEmpty())
            jugadorDataResponse.setEditable(false);
        else {
            List<Asistente> asistentes=asistenteRepository.findAllByJugador(jugadorRepository.getById(id));
            for (Asistente asistente: asistentes) {
                if(!apuestaRepository.findAllByUser(userRepository.getById(asistente.getId())).isEmpty()){
                    jugadorDataResponse.setEditable(false);
                    break;
                }
            }
        }
        return jugadorDataResponse;
    }


    @GetMapping("/temporal/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> updateApuestas(@PathVariable Long id) {
        Sorteo sorteo = sorteoRepository.getSorteoById(id);
        sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
        sorteoRepository.save(sorteo);
        return ResponseEntity.ok("Update apuestas");
    }

    @GetMapping("/temporal/reset/balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> resetBalance() {
        Util.updateJugadoBalance(jugadorRepository);
        return ResponseEntity.ok("Update apuestas");
    }

    @GetMapping("/temporal/crear/apuesta")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> insertApuesta() {
        TimeDate time = TimeDate.getInstance();
        LocalDate ld = LocalDate.now(time.getZ());
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoActivoBySorteoTime(new Timestamp(
                ZonedDateTime.of(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), 21, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));
        if (sorteoDiaria != null) {
            Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
            apuestas.forEach(apuesta -> apuestaRepository.delete(apuesta));
            sorteoDiariaRepository.delete(sorteoDiaria);
        }
        Util.insertSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, sorteoTypeRepository, SorteoTypeName.DIARIA, 21);

        return ResponseEntity.ok("Update apuestas");
    }

    @GetMapping("/temporal/crear/apuesta/chica")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> insertApuestaChica() {
        TimeDate time = TimeDate.getInstance();
        LocalDate ld = LocalDate.now(time.getZ());
        SorteoDiaria sorteoDiaria = getThisWeekChica();

        if (sorteoDiaria != null) {
            Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
            apuestas.forEach(apuesta -> apuestaRepository.delete(apuesta));
            List<Resultado> resultados = resultadoRepository.findAllBySorteo(sorteoDiaria.getSorteo());
            resultados.forEach(resultado -> resultadoRepository.delete(resultado));
            NumeroGanador numeroGanador = numeroGanadorRepository.getBySorteo(sorteoDiaria.getSorteo());
            numeroGanadorRepository.delete(numeroGanador);
            sorteoDiariaRepository.delete(sorteoDiaria);
            sorteoRepository.delete(sorteoDiaria.getSorteo());
        }
        Util.insertSorteo(estadoRepository, sorteoRepository, sorteoDiariaRepository, sorteoTypeRepository, SorteoTypeName.CHICA, 1);
        return ResponseEntity.ok("Update apuestas");
    }

    @GetMapping("/jugadores/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public Long countJugadores() {
        return playerCountRepository.findAll().get(0).getCount() + 1;
    }

    @GetMapping("/jugadores/delete/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> deleteJugador(@PathVariable Long id) {
        User user = userRepository.getById(id);

        if (apuestaRepository.findAllByUser(user).size() > 0) {
            return ResponseEntity.ok("Apuestas");
        } else {
            if (user instanceof Jugador) {
                List<Asistente> asistentes = asistenteRepository.findAllByJugador((Jugador) user);
                if (asistentes.stream().anyMatch(asistente ->
                        apuestaRepository.findAllByUser(asistente).size() > 0))
                    return ResponseEntity.ok("Apuestas");
                if (asistentes.size() > 0)
                    userRepository.deleteAll(asistentes);
            }
        }
        userRepository.delete(user);
        return ResponseEntity.ok("Usuario eliminado");
    }

    @PostMapping("/jugadores/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> addNewJugador(@Valid @RequestBody
                                                        CreatePlayerForm createPlayerForm) {
        double comisionChicaDirecto = 0;
        double premioChicaDirecto = 0;

        double costoChicaMiles = 0;
        double premioChicaMiles = 0;

        double comisionChicaPedazos = 0;
        double costoChicaPedazos = 0;
        double premioChicaPedazos = 0;

        double comisionDirecto = 0;
        double premioDirecto = 0;

        double costoMil = 0;
        double premioMil = 0;

        if (createPlayerForm.getUtype().equals("p")) {
            if (userRepository.existsByUsername(createPlayerForm.getUsername())) {
                return new ResponseEntity<String>("Fail -> Username is already taken!",
                        HttpStatus.BAD_REQUEST);
            }
            Moneda moneda = monedaRepository.findByMonedaName(MonedaName.DOLAR);
            if (createPlayerForm.getMtype().equals("l")) {
                moneda = monedaRepository.findByMonedaName(MonedaName.LEMPIRA);
            }
            TipoApostador tipoApostador = tipoApostadorRepository
                    .findByApostadorName(ApostadorName.DIRECTO);
            if (createPlayerForm.getDtype().equals("dm")) {
                tipoApostador = tipoApostadorRepository.findByApostadorName(ApostadorName.MILES);
                costoMil = Double.valueOf(createPlayerForm.getDparam1());
                premioMil = Double.valueOf(createPlayerForm.getDparam2());
            } else {
                comisionDirecto = Double.valueOf(createPlayerForm.getDparam1());
                premioDirecto = Double.valueOf(createPlayerForm.getDparam2());
            }
            TipoChica tipoChica = tipoChicaRepository.findByChicaName(ChicaName.DIRECTO);
            switch (createPlayerForm.getCtype()) {
                case "cm":
                    tipoChica = tipoChicaRepository.findByChicaName(ChicaName.MILES);
                    costoChicaMiles = Double.valueOf(createPlayerForm.getCparam1());
                    premioChicaMiles = Double.valueOf(createPlayerForm.getCparam2());
                    break;
                case "cp":
                    tipoChica = tipoChicaRepository.findByChicaName(ChicaName.PEDAZOS);
                    comisionChicaPedazos = Double.valueOf(createPlayerForm.getCparam1());
                    costoChicaPedazos = Double.valueOf(createPlayerForm.getCparam2());
                    premioChicaPedazos = Double.valueOf(createPlayerForm.getCparam3());
                    break;
                default:
                    comisionChicaDirecto = Double.valueOf(createPlayerForm.getCparam1());
                    premioChicaDirecto = Double.valueOf(createPlayerForm.getCparam2());
                    break;
            }
            Jugador jugador = new Jugador(createPlayerForm.getName(),
                    createPlayerForm.getUsername(),
                    encoder.encode(createPlayerForm.getPassword()),
                    moneda, tipoApostador, tipoChica);

            Set<Role> roles2 = new HashSet<>();
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException(
                            "Fail! -> Cause: Master Role not find."));
            roles2.add(userRole);
            jugador.setRoles(roles2);

            jugador.setCostoMil(costoMil);
            jugador.setPremioMil(premioMil);

            jugador.setComisionDirecto(comisionDirecto);
            jugador.setPremioDirecto(premioDirecto);

            jugador.setComisionChicaDirecto(comisionChicaDirecto);
            jugador.setPremioChicaDirecto(premioChicaDirecto);

            jugador.setCostoChicaMiles(costoChicaMiles);
            jugador.setPremioChicaMiles(premioChicaMiles);

            jugador.setComisionChicaPedazos(comisionChicaPedazos);
            jugador.setCostoChicaPedazos(costoChicaPedazos);
            jugador.setPremioChicaPedazos(premioChicaPedazos);

            jugadorRepository.save(jugador);

            List<PlayerCount> playerCounts = playerCountRepository.findAll();
            if (!playerCounts.isEmpty()) {
                PlayerCount playerCount = playerCounts.get(0);
                Long count = playerCount.getCount() + 1L;
                playerCount.setCount(count);
                playerCountRepository.save(playerCount);
            } else {
                PlayerCount playerCount = new PlayerCount();
                playerCount.setCount(1L);
                playerCountRepository.save(playerCount);
            }

        }
        return ResponseEntity.ok().body("User registered successfully!");
    }

    @PostMapping("/jugadores/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> updateJugadorData(@Valid @RequestBody
                                                            CreatePlayerForm createPlayerForm) {
        double comisionChicaDirecto = 0;
        double premioChicaDirecto = 0;

        double costoChicaMiles = 0;
        double premioChicaMiles = 0;

        double comisionChicaPedazos = 0;
        double costoChicaPedazos = 0;
        double premioChicaPedazos = 0;

        double comisionDirecto = 0;
        double premioDirecto = 0;

        double costoMil = 0;
        double premioMil = 0;

        Jugador jugador = (Jugador) userRepository.getByUsername(createPlayerForm.getUsername());

        Moneda moneda = monedaRepository.findByMonedaName(MonedaName.DOLAR);
        if (createPlayerForm.getMtype().equals("l")) {
            moneda = monedaRepository.findByMonedaName(MonedaName.LEMPIRA);
        }
        TipoApostador tipoApostador = tipoApostadorRepository
                .findByApostadorName(ApostadorName.DIRECTO);
        if (createPlayerForm.getDtype().equals("dm")) {
            tipoApostador = tipoApostadorRepository.findByApostadorName(ApostadorName.MILES);
            costoMil = Double.valueOf(createPlayerForm.getDparam1());
            premioMil = Double.valueOf(createPlayerForm.getDparam2());
        } else {
            comisionDirecto = Double.valueOf(createPlayerForm.getDparam1());
            premioDirecto = Double.valueOf(createPlayerForm.getDparam2());
        }
        TipoChica tipoChica = tipoChicaRepository.findByChicaName(ChicaName.DIRECTO);
        switch (createPlayerForm.getCtype()) {
            case "cm":
                tipoChica = tipoChicaRepository.findByChicaName(ChicaName.MILES);
                costoChicaMiles = Double.valueOf(createPlayerForm.getCparam1());
                premioChicaMiles = Double.valueOf(createPlayerForm.getCparam2());
                break;
            case "cp":
                tipoChica = tipoChicaRepository.findByChicaName(ChicaName.PEDAZOS);
                comisionChicaPedazos = Double.valueOf(createPlayerForm.getCparam1());
                costoChicaPedazos = Double.valueOf(createPlayerForm.getCparam2());
                premioChicaPedazos = Double.valueOf(createPlayerForm.getCparam3());
                break;
            default:
                comisionChicaDirecto = Double.valueOf(createPlayerForm.getCparam1());
                premioChicaDirecto = Double.valueOf(createPlayerForm.getCparam2());
                break;
        }
        jugador.setName(createPlayerForm.getName());
        if(!createPlayerForm.getPassword().equals("1") && createPlayerForm.getPassword() !=null)
            jugador.setPassword(encoder.encode(createPlayerForm.getPassword()));
        jugador.setMoneda(moneda);
        jugador.setTipoApostador(tipoApostador);
        jugador.setTipoChica(tipoChica);

        jugador.setCostoMil(costoMil);
        jugador.setPremioMil(premioMil);

        jugador.setComisionDirecto(comisionDirecto);
        jugador.setPremioDirecto(premioDirecto);

        jugador.setComisionChicaDirecto(comisionChicaDirecto);
        jugador.setPremioChicaDirecto(premioChicaDirecto);

        jugador.setCostoChicaMiles(costoChicaMiles);
        jugador.setPremioChicaMiles(premioChicaMiles);

        jugador.setComisionChicaPedazos(comisionChicaPedazos);
        jugador.setCostoChicaPedazos(costoChicaPedazos);
        jugador.setPremioChicaPedazos(premioChicaPedazos);
        jugadorRepository.save(jugador);

        return ResponseEntity.ok().body("User updated successfully!");
    }

    @PostMapping("/jugador/asistentes/count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public Integer countJugadorAsistente(@Valid @RequestBody UserIdForm userIdForm) {
        return asistenteRepository.findAllByJugador(jugadorRepository
                .getById(userIdForm.getId())).size();

    }

    @PostMapping("/asistente/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> addNewAsistente(@Valid
                                                  @RequestBody CreateAsistForm createAsistForm) {

        Jugador jugador = jugadorRepository.getOne(createAsistForm.getPlayerId());
        Set<Role> roles2 = new HashSet<>();
        Role asisRole = roleRepository.findByName(RoleName.ROLE_ASIS)
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: Asist Role not find."));
        roles2.add(asisRole);
        Asistente asistente = new Asistente(createAsistForm.getName(),
                createAsistForm.getUsername(),
                encoder.encode(createAsistForm.getPassword()), jugador);
        asistente.setRoles(roles2);
        asistenteRepository.save(asistente);
        return ResponseEntity.ok().body("Asistente registered successfully!");
    }

    @PostMapping("/asistente/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> updateJugadorData(@Valid @RequestBody
                                                            CreateAsistForm createAsistForm) {
        Asistente asistente = (Asistente) userRepository.getByUsername(createAsistForm.getUsername());

        if (!createAsistForm.getPassword().equals("123456789"))
            asistente.setPassword(encoder.encode(createAsistForm.getPassword()));

        asistente.setName(createAsistForm.getName());
        asistenteRepository.save(asistente);
        return ResponseEntity.ok("Update asistente");
    }

    @GetMapping("/moneda/cambio/current")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public Double getCurrentCambio() {
        return cambioRepository
                .findFirstByOrderByIdDesc().getCambio();
    }

    @PostMapping("/moneda/cambio/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> updateCambio(@Valid @RequestBody CambioForm cambioForm) {
        Cambio cambio = new Cambio();
        cambio.setCambio(cambioForm.getCambio());
        cambio.setCambioTime(new Timestamp(ZonedDateTime.now().toInstant().toEpochMilli()));
        cambioRepository.save(cambio);
        return ResponseEntity.ok(new CambioResponse(cambioForm.getCambio()));
    }

    @GetMapping("/numeros/fijados")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('USER') or hasRole('ASIS')")
    public List<TopeResponse> getListTope() {
        List<TopeResponse> topes = new ArrayList<>();
        List<Restriccion> restriciones = restriccionRepository
                .findAllByTimestamp(Util.getTodayTimeStamp());
        for (int i = 0; i < 100; i++) {
            boolean flag = false;
            for (Restriccion restriccion : restriciones) {
                if (restriccion.getNumero() == i) {
                    if (i < 10) {
                        topes.add(new TopeResponse("0" + i, restriccion.getPuntos()));
                    } else {
                        topes.add(new TopeResponse(String.valueOf(i), restriccion.getPuntos()));
                    }
                    flag = true;
                }
            }
            if (!flag && i < 10) {
                topes.add(new TopeResponse("0" + i, 0));
            } else if (!flag) {
                topes.add(new TopeResponse(String.valueOf(i), 0));
            }
        }
        return topes;
    }

    @PostMapping("/numeros/fijados/add")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER') or hasRole('USER') or hasRole('ASIS')")
    public ResponseEntity<?> fijarNumero(@Valid @RequestBody FijarForm fijarForm) {
        Integer value = Integer.valueOf(fijarForm.getNumero());
        Restriccion restriccion = new Restriccion();
        if (restriccionRepository.existsByNumero(value)) {
            restriccion = restriccionRepository.findByNumero(value);
        } else {
            restriccion.setNumero(value);
            restriccion.setTimestamp(Util.getTodayTimeStamp());
        }
        restriccion.setPuntos(fijarForm.getPuntos());
        restriccion.setPuntosCurrent(0);
        restriccionRepository.save(restriccion);
        return ResponseEntity.ok(new SimpleResponse("Numero Fijado"));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<String> getAllUsers() {
        List<User> users = userRepository.findByOrderByIdAsc();
        List<String> userShortResponses = new ArrayList<>();
        for (User user : users) {
            userShortResponses.add(user.getUsername());
        }
        return userShortResponses;
    }


    @PostMapping("/user/password/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> updatePasswword(@Valid @RequestBody LoginForm loginForm) {
        User user = userRepository.findByUsername(loginForm.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with -> username : "
                                + loginForm.getUsername())
                );
        user.setPassword(encoder.encode(loginForm.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new SimpleResponse("Password update"));
    }

    @GetMapping("/asistente/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public AsistenteEditarResponse getAsistenteDetails(@PathVariable Long id) {
        AsistenteEditarResponse asistenteEditarResponse=new AsistenteEditarResponse();
        User user = userRepository.getById(id);
        asistenteEditarResponse.setId(id);
        asistenteEditarResponse.setName(user.getName());
        asistenteEditarResponse.setUsername(user.getUsername());
        asistenteEditarResponse.setJugadorName(((Asistente) user).getJugador().getName());
        asistenteEditarResponse.setJugadorUsername(((Asistente) user).getJugador().getUsername());
        asistenteEditarResponse.setJugadorMoneda(((Asistente) user).getJugador().getMoneda().getMonedaName().toString());
        return asistenteEditarResponse;
    }

    @PostMapping("/historial/balance")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public Double getBalanceBetweenDates(@Valid @RequestBody ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        Date inicio = mapper.convertValue(json.get("inicio"), Date.class);
        Date fin = mapper.convertValue(json.get("fin"), Date.class);
        Timestamp tsInicio = new Timestamp(inicio.getTime());
        Timestamp tsFin = new Timestamp(fin.getTime());
        List<Sorteo> sorteos = sorteoRepository
                .getAllBetweenTimestamp(tsInicio, tsFin);
        double[] total = {0.0};
        sorteos.forEach(sorteo -> {
            if (sorteo.getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
                List<Resultado> resultados = resultadoRepository.findAllBySorteo(sorteo);
                resultados.forEach(resultado -> {
                    total[0] += resultado.getPremio() + resultado.getComision() - resultado.getCantApuesta();
                });
            }
        });
        return total[0];
    }

    @PostMapping("/historial/week/current/usuario/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public HistorialSemanalUserResponse getCurrentWeekApuestasHistorialForUser(@PathVariable Long id,
                                                                               @Valid @RequestBody ObjectNode json) {

        HistorialSemanalUserResponse historialSemanalUserResponse = new HistorialSemanalUserResponse();
        ObjectMapper mapper = new ObjectMapper();
        String type = mapper.convertValue(json.get("type"), String.class);
        List<Sorteo> sorteos = sorteoRepository.findAllByStatus(statusRepository
                .getByStatus(StatusName.CURRENT));
        Jugador jugador = (Jugador) userRepository.getById(id);
        List<Resultado> resultados = new ArrayList<>();
        sorteos.forEach(sorteo -> {
            resultados.add(resultadoRepository.getBySorteoAndUser(sorteo, jugador));
        });
        boolean[] resultadoCheck = new boolean[resultados.size()];
        Map<String, Double> map = new HashMap<>();
        for (int i = 0; i < resultados.size(); i++) {
            Resultado resultado = resultados.get(i);
            resultadoCheck[i] = true;
            String title = Util.shortTimestamp(resultado.getSorteo().getSorteoTime());
            double cambio = 1;
            if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.DOLAR))
                    && type.equals("lempira")) {
                cambio = resultado.getCambio().getCambio();
            } else if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.LEMPIRA))
                    && type.equals("dolar")) {
                cambio = 1 / resultado.getCambio().getCambio();
            }
            double balance = (resultado.getPremio() - resultado.getCantApuesta() - resultado.getComision()) * cambio;
            if (!map.containsKey(title)) {
                map.put(title, balance);
            }
            for (int j = 0; j < resultados.size(); j++) {
                if (j != i && !resultadoCheck[j]
                        && Util.compareToTimestamp(resultado.getSorteo().getSorteoTime(),
                        resultados.get(j).getSorteo().getSorteoTime())) {
                    resultadoCheck[j] = true;
                    cambio = 1;
                    if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.DOLAR))
                            && type.equals("lempira")) {
                        cambio = resultados.get(j).getCambio().getCambio();
                    } else if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.LEMPIRA))
                            && type.equals("dolar")) {
                        cambio = 1 / resultados.get(j).getCambio().getCambio();
                    }
                    balance = cambio * (resultados.get(j).getPremio()
                            - resultados.get(j).getCantApuesta()
                            - resultados.get(j).getComision());
                    balance += map.get(title);
                    map.replace(title, balance);
                }
            }
        }
        List<TripletUDD> uddList = new ArrayList<>();
        double[] total = {0.0};
        map.forEach((s, aDouble) -> {
            TripletUDD tripletUDD = new TripletUDD();
            tripletUDD.setBalance(aDouble);
            tripletUDD.setDate(s);
            tripletUDD.setTitle(Util.formatStringShortData2StringShort(s));
            uddList.add(tripletUDD);
            total[0] += aDouble;
        });

        historialSemanalUserResponse.setBalance(total[0]);
        historialSemanalUserResponse.setUddList(uddList);
        return historialSemanalUserResponse;
    }

    @PostMapping("/historial/week/current/usuario/{id}/detail")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<ResultadoResponse> getCurrentWeekApuestasHistorialForUserDetails(@PathVariable Long id,
                                                                                 @Valid @RequestBody ObjectNode json) {

        List<ResultadoResponse> responses = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        String type = mapper.convertValue(json.get("type"), String.class);
        String date = mapper.convertValue(json.get("date"), String.class);
        List<Sorteo> sorteos = sorteoRepository.findAllByStatus(statusRepository
                .getByStatus(StatusName.CURRENT));
        Jugador jugador = (Jugador) userRepository.getById(id);
        List<Resultado> resultados = new ArrayList<>();
        sorteos.forEach(sorteo -> {
            resultados.add(resultadoRepository.getBySorteoAndUser(sorteo, jugador));
        });
        resultados.forEach(resultado -> {
            if (Util.compareStringToTimestamp(date, resultado.getSorteo().getSorteoTime())) {
                ResultadoResponse resultadoResponse = new ResultadoResponse();
                resultadoResponse.setTitle(Util.formatTimestamp2String(resultado.getSorteo().getSorteoTime()));
                resultadoResponse.setTotal(resultado.getCantApuesta());
                resultadoResponse.setComision(resultado.getComision());
                resultadoResponse.setPremio(resultado.getPremio());
                resultadoResponse.setId(resultado.getSorteo().getId());
                responses.add(resultadoResponse);
            }
        });
        return responses;
    }

    @PostMapping("/historial/week/current")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResumenSemanalResponse getCurrentWeekApuestasHistorial(@Valid
                                                                  @RequestBody ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String type = mapper.convertValue(json.get("type"), String.class);
        ResumenSemanalResponse resumenSemanalResponse = new ResumenSemanalResponse();
        List<Sorteo> sorteos = sorteoRepository.findAllByStatus(statusRepository
                .getByStatus(StatusName.CURRENT));
        Iterable<SorteoDiaria> sorteoDiarias = sorteoDiariaRepository.findAll();
        List<PairJB> pairJBS = new ArrayList<>();
        final double[] total = {0.0};
        final double[] comision = {0.0};
        final double[] premio = {0.0};
        sorteos.forEach(sorteo -> {
            boolean flag = false;
            for (SorteoDiaria sorteoDiaria : sorteoDiarias) {
                if (!sorteo.getEstado().getEstado().equals(EstadoName.ABIERTA)
                        && sorteoDiaria.getId().equals(sorteo.getId())) {
                    flag = true;
                }
            }
            if (!flag) {
                List<Resultado> resultados = resultadoRepository.findAllBySorteo(sorteo);
                resultadosFillData(pairJBS, total, comision, premio, resultados, type);
            }
        });

        final double[] totalDiaria = {0.0};
        final double[] comisionDiaria = {0.0};
        double[] cantidad = new double[100];
        double[] riesgo = new double[100];
        sorteoDiarias.forEach(sorteoDiaria -> {
            Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
            if (sorteo.getEstado().getEstado().equals(EstadoName.ABIERTA)||
                    sorteo.getEstado().getEstado().equals(EstadoName.CERRADA)) {
                Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);

                apuestaList.forEach(apuesta -> {
                    User user= apuesta.getUser();
                    Jugador jugador = null;
                    double cambio = 1;

                    if(user instanceof Jugador){
                        jugador = (Jugador) user;
                    }else{
                        jugador = ((Asistente) user).getJugador();
                    }
                    double costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 ;
                    double costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1 ;
                    double costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1 ;


                    if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.DOLAR))
                            && type.equals("lempira")) {
                        cambio = apuesta.getCambio().getCambio();
                    } else if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.LEMPIRA))
                            && type.equals("dolar")) {
                        cambio = 1 / apuesta.getCambio().getCambio();
                    }

                    double premioData = 0.0;
                    int numero = apuesta.getNumero();
                    if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
                        if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
                            premioData = jugador.getPremioDirecto() * apuesta.getCantidad() * cambio;
                        } else {
                            premioData = jugador.getPremioMil() * 1000 * apuesta.getCantidad() * cambio;
                        }
                    } else {
                        if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
                            premioData = jugador.getPremioChicaDirecto() * apuesta.getCantidad() * cambio;
                        } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
                            premioData = jugador.getPremioChicaMiles() * 1000 * apuesta.getCantidad() * cambio;
                        } else {
                            premioData = jugador.getPremioChicaPedazos() * apuesta.getCantidad() * cambio;
                        }
                    }
                    cantidad[numero] += apuesta.getCantidad() * cambio * costoMilDiaria * costoMilChica * costoPedazoChica;
                    riesgo[numero] += premioData;
                    totalDiaria[0] += apuesta.getCantidad() * cambio * costoMilDiaria * costoMilChica * costoPedazoChica;
                    comisionDiaria[0] += apuesta.getComision() * cambio;
                });
            }

        });
        double max = 0.0;
        int pos = -1;
        for (int i = 0; i < 100; i++) {
            if (cantidad[i] != 0) {
                if (max < riesgo[i]) {
                    max = riesgo[i];
                    pos = i;
                }
            }
        }
        RiesgoMaximo riesgoMaximo = new RiesgoMaximo();

        if (pos != -1) {
            riesgoMaximo.setNumero(pos);
            riesgoMaximo.setTotalValor(riesgo[pos]);
            riesgoMaximo.setValor(cantidad[pos]);
            riesgoMaximo.setComision(total[0] / riesgo[pos]);
        }
        resumenSemanalResponse.setTitle(Util.getMondayAndSundayDay());
        resumenSemanalResponse.setTotalSemana(total[0]);
        resumenSemanalResponse.setTotalPremio(premio[0]);
        resumenSemanalResponse.setComisionesSemana(comision[0]);
        resumenSemanalResponse.setEntradaNetaSemana(total[0] - comision[0]);
        resumenSemanalResponse.setBalance((total[0] - comision[0]) - premio[0]);
        resumenSemanalResponse.setTotalToday(totalDiaria[0]);
        resumenSemanalResponse.setComisionToday(comisionDiaria[0]);
        resumenSemanalResponse.setEntradaNetaToday(totalDiaria[0] - comisionDiaria[0]);
        resumenSemanalResponse.setPairJBList(pairJBS);
        resumenSemanalResponse.setRiesgoMaximo(riesgoMaximo);
        return resumenSemanalResponse;
    }


    @PostMapping("/historial/week/last")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResumenSemanaAnteriorResponse
    getLastWeekApuestasHistorial(@Valid @RequestBody ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String type = mapper.convertValue(json.get("type"), String.class);
        ResumenSemanaAnteriorResponse resumenSemanaAnteriorResponse =
                new ResumenSemanaAnteriorResponse();
        List<Sorteo> sorteos = sorteoRepository
                .findAllByStatus(statusRepository.getByStatus(StatusName.LAST));
        List<PairJB> pairJBS = new ArrayList<>();
        final double[] total = {0.0};
        final double[] comision = {0.0};
        final double[] premio = {0.0};
        sorteos.forEach(sorteo -> {
            List<Resultado> resultados = resultadoRepository.findAllBySorteo(sorteo);
            resultadosFillData(pairJBS, total, comision, premio, resultados, type);
        });
        resumenSemanaAnteriorResponse.setTitle(Util.getLastWeekMondayAndSunday());
        resumenSemanaAnteriorResponse.setTotalSemana(total[0]);
        resumenSemanaAnteriorResponse.setTotalPremio(premio[0]);
        resumenSemanaAnteriorResponse.setComisionesSemana(comision[0]);
        resumenSemanaAnteriorResponse.setEntradaNetaSemana(total[0] - comision[0]);
        resumenSemanaAnteriorResponse.setBalance((total[0] - comision[0]) - premio[0]);
        resumenSemanaAnteriorResponse.setPairJBList(pairJBS);
        return resumenSemanaAnteriorResponse;
    }

    @GetMapping("/historial/numeros/ganadores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<NumeroGanadorResponse> getLast30SorteosNumeroGanador() {
        List<NumeroGanadorResponse> ganadorResponseList = new ArrayList<>();

        List<NumeroGanador> numeroGanadors = numeroGanadorRepository.findTop30ByOrderByIdDesc();
        numeroGanadors.forEach(numeroGanador -> {
            List<Resultado> resultados = resultadoRepository.findAllBySorteo(numeroGanador.getSorteo());
            List<PairJB> pairJBS = new ArrayList<>();
            resultados.forEach(resultado -> {
                Jugador jugador = (Jugador) resultado.getUser();
                double balanceJugador = resultado.getPremio() + resultado.getComision()
                        - resultado.getCantApuesta();
                pairJBS.add(new PairJB(jugador.getUsername(), balanceJugador,
                        jugador.getMoneda().getMonedaName().toString()));
            });
            NumeroGanadorResponse ganadorResponse = new NumeroGanadorResponse();
            ganadorResponse.setNumero(numeroGanador.getNumeroGanador());
            ganadorResponse.setPairJBS(pairJBS);
            ganadorResponse.setTitle(Util.formatTimestamp2String(numeroGanador.getSorteo().getSorteoTime()));
            ganadorResponse.setId(numeroGanador.getSorteo().getId());
            ganadorResponseList.add(ganadorResponse);
        });
        return ganadorResponseList;
    }

    @GetMapping("/numeros/ganadores")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<ApuestaNumeroGanadorResponse> getApuestasToday() {
        List<ApuestaNumeroGanadorResponse> ganadorResponses = new ArrayList<>();
        Iterable<SorteoDiaria> sorteoDiarias = sorteoDiariaRepository.findAll();
        sorteoDiarias.forEach(sorteoDiaria -> {
            Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
            ApuestaNumeroGanadorResponse ganadorResponse = new ApuestaNumeroGanadorResponse();
            Integer numero = -1;
            if (sorteo.getEstado().getEstado().equals(EstadoName.CERRADA)) {
                ganadorResponse.setStatus("cerrada");
            } else if (sorteo.getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
                numero = numeroGanadorRepository.getBySorteo(sorteo).getNumeroGanador();
                ganadorResponse.setStatus("bloqueada");
            } else {
                ganadorResponse.setStatus("abierta");
            }
            ganadorResponse.setType(sorteo.getSorteoType().getSorteoTypeName().toString());
            ganadorResponse.setSorteId(sorteo.getId());
            ganadorResponse.setNumero(numero);
            ganadorResponse.setTitle(Util.formatTimestamp2StringShortAbb(sorteoDiaria.getSorteoTime()));
            ganadorResponse.setSorteId(sorteoDiaria.getId());
            ganadorResponses.add(ganadorResponse);
        });
        return ganadorResponses;
    }

    @PostMapping("/numeros/ganadores/{id}/update")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> updateNumeroGanadarByApuestaId(
            @PathVariable Long id,
            @Valid @RequestBody ObjectNode jsonNodes
    ) {
        ObjectMapper mapper = new ObjectMapper();
        Integer numero = mapper.convertValue(jsonNodes.get("numero"), Integer.class);
        Integer numeroOld = mapper.convertValue(jsonNodes.get("anterior"), Integer.class);

        Sorteo sorteo = sorteoRepository.getSorteoById(id);

        List<HistoricoApuestas> historicoApuestas = historicoApuestaRepository
                .findAllBySorteoAndNumero(sorteo, numeroOld);
        historicoApuestas.forEach(historicoApuestas1 -> {
            User user = historicoApuestas1.getUser();
            if (user instanceof Jugador) {
                Resultado resultado = resultadoRepository.getBySorteoAndUser(sorteo, user);
                resultado.setPremio(0);
                resultadoRepository.save(resultado);
            }
        });
        historicoApuestas = historicoApuestaRepository.findAllBySorteoAndNumero(sorteo, numero);
        historicoApuestas.forEach(historicoApuestas1 -> {
            User user = historicoApuestas1.getUser();

            if (user instanceof Jugador) {
                double premio = 0.0;
                Jugador jugador = (Jugador) user;
                if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
                    if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.MILES)) {
                        premio = historicoApuestas1.getNumero() * 1000 * jugador.getPremioMil();
                    } else {
                        premio = historicoApuestas1.getNumero() * jugador.getPremioDirecto();
                    }
                } else {
                    if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
                        premio = historicoApuestas1.getNumero() * jugador.getPremioChicaDirecto();
                    } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
                        premio = historicoApuestas1.getNumero() * 1000 * jugador.getPremioChicaMiles();
                    } else {
                        premio = historicoApuestas1.getNumero() * jugador.getPremioChicaPedazos();
                    }
                }
                Resultado resultado = resultadoRepository.getBySorteoAndUser(sorteo, user);
                resultado.setPremio(premio);
                resultadoRepository.save(resultado);
            }
        });
        NumeroGanador numeroGanador = numeroGanadorRepository.getBySorteo(sorteo);
        numeroGanador.setNumeroGanador(numero);
        numeroGanadorRepository.save(numeroGanador);
        return ResponseEntity.ok("Numero fijado");
    }

    @PostMapping("/numero/ganador/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<String> setNumeroGanadarByApuestaId(
            @PathVariable Long id,
            @Valid @RequestBody ObjectNode jsonNodes
    ) {
        Sorteo sorteo = sorteoRepository.getSorteoById(id);
        ObjectMapper mapper = new ObjectMapper();
        Integer numero = mapper.convertValue(jsonNodes.get("numero"), Integer.class);
        Optional<NumeroGanador> numeroGanadorOption=numeroGanadorRepository.getNumeroGanadorBySorteo(sorteo);
        NumeroGanador numeroGanador;
        if (numeroGanadorOption.isPresent()) {
            numeroGanador = numeroGanadorOption.get();
            List<HistoricoApuestas> historicoApuestas = historicoApuestaRepository
                    .findAllBySorteoAndNumero(sorteo, numeroGanador.getNumeroGanador());
            historicoApuestas.forEach(historicoApuestas1 -> {
                User user = historicoApuestas1.getUser();
                if (user instanceof Jugador) {
                    Resultado resultado = resultadoRepository.getBySorteoAndUser(sorteo, user);
                    resultado.setPremio(0);
                    resultadoRepository.save(resultado);
                }
            });
            historicoApuestas = historicoApuestaRepository.findAllBySorteoAndNumero(sorteo, numero);
            historicoApuestas.forEach(historicoApuestas1 -> {
                User user = historicoApuestas1.getUser();
                if (user instanceof Jugador) {
                    double premio = 0.0;
                    Jugador jugador = (Jugador) user;
                    if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
                        if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.MILES)) {
                            premio = historicoApuestas1.getNumero() * 1000 * jugador.getPremioMil();
                        } else {
                            premio = historicoApuestas1.getNumero() * jugador.getPremioDirecto();
                        }
                    } else {
                        if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
                            premio = historicoApuestas1.getNumero() * jugador.getPremioChicaDirecto();
                        } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
                            premio = historicoApuestas1.getNumero() * 1000 * jugador.getPremioChicaMiles();
                        } else {
                            premio = historicoApuestas1.getNumero() * jugador.getPremioChicaPedazos();
                        }
                    }
                    Resultado resultado = resultadoRepository.getBySorteoAndUser(sorteo, user);
                    resultado.setPremio(premio);
                    resultadoRepository.save(resultado);
                }
            });
            numeroGanador.setNumeroGanador(numero);
        } else {
            numeroGanador = new NumeroGanador();
            numeroGanador.setNumeroGanador(numero);
            numeroGanador.setSorteo(sorteo);
            Map<String, double[]> map = new HashMap<>();
            List<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaOrderByUserDesc(
                    sorteoDiariaRepository.getSorteoDiariaById(id));
            apuestaList.forEach(apuesta -> {
                User user = apuesta.getUser();
                String username = user.getUsername();
                double[] aux = new double[100];
                int pos = apuesta.getNumero();
                Jugador jugador=null;
                if (user instanceof Jugador) {
                    jugador = (Jugador) user;
                } else if (user instanceof Asistente) {
                    jugador = ((Asistente) user).getJugador();
                }
                handleMapForUsers(map, apuesta, jugador, aux, pos, sorteo);
            });
            if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
                map.forEach((s, doubles) -> {
                    User user = userRepository.getByUsername(s);
                    Jugador jugador = (Jugador) user;

                    double premio = 0;
                    if (doubles[numero] != 0) {
                        premio = jugador.getPremioMil() * 1000 * doubles[numero];
                        if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
                            premio = jugador.getPremioDirecto() * doubles[numero];
                        }
                    }
                    double total = 0.0;
                    for (double aDouble : doubles) {
                        total += aDouble;
                    }
                    double comision = 0;
                    if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
                        comision = total * jugador.getComisionDirecto() / 100;
                    }
                    Resultado resultado = new Resultado();
                    resultado.setCantApuesta(total);
                    resultado.setComision(comision);
                    resultado.setPremio(premio);
                    resultado.setSorteo(sorteo);
                    resultado.setUser(user);
                    resultado.setCambio(apuestaRepository.findAllBySorteoDiariaAndUserOrderByNumeroAsc(
                            sorteoDiariaRepository.getSorteoDiariaById(id), user
                    ).get(0).getCambio());
                    resultadoRepository.save(resultado);
                    double balance = jugador.getBalance() + (premio + comision - total);
                    jugador.setBalance(balance);
                    jugadorRepository.save(jugador);
                });
            } else {
                map.forEach((s, doubles) -> {
                    User user = userRepository.getByUsername(s);
                    Jugador jugador = (Jugador) user;
                    double premio = 0;
                    if (doubles[numero] != 0) {
                        premio = jugador.getPremioMil() * 1000 * doubles[numero];
                        if (jugador.getTipoChica().getChicaName().equals(ChicaName.PEDAZOS)) {
                            premio = doubles[numero] * jugador.getPremioChicaPedazos();
                        } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
                            premio = jugador.getPremioDirecto() * doubles[numero];
                        }
                    }
                    double total = 0.0;
                    for (double aDouble : doubles) {
                        total += aDouble;
                    }
                    double comision = 0;
                    if (jugador.getTipoChica().getChicaName().equals(ChicaName.PEDAZOS)) {
                        comision = (total * jugador.getCostoChicaPedazos()) * jugador.getComisionChicaPedazos();
                    } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
                        comision = total * ((Jugador) user).getComisionDirecto() / 100;
                    }
                    Resultado resultado = new Resultado();
                    resultado.setCantApuesta(total);
                    resultado.setComision(comision);
                    resultado.setPremio(premio);
                    resultado.setSorteo(sorteo);
                    resultado.setUser(user);
                    resultado.setCambio(apuestaRepository.findAllBySorteoDiariaAndUserOrderByNumeroAsc(
                            sorteoDiariaRepository.getSorteoDiariaById(id), user
                    ).get(0).getCambio());
                    resultadoRepository.save(resultado);
                    double balance = jugador.getBalance() + (premio + comision - total);
                    jugador.setBalance(balance);
                    jugadorRepository.save(jugador);
                });
            }
            sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.BLOQUEADA));
            apuestaList.forEach(apuesta->{
                HistoricoApuestas historicoApuestas = new HistoricoApuestas();
                historicoApuestas.setCantidad(apuesta.getCantidad());
                historicoApuestas.setUser(apuesta.getUser());
                historicoApuestas.setSorteo(sorteo);
                historicoApuestas.setNumero(apuesta.getNumero());
                historicoApuestas.setComision(apuesta.getComision());
                historicoApuestas.setCambio(apuesta.getCambio());
                historicoApuestas.setDate(apuesta.getDate());
                Jugador jugador = Util.getJugadorFromApuesta(apuesta);
                double cantidadMultiplier = MathUtil.getCantidadMultiplier(jugador, apuesta, sorteo.getSorteoType().getSorteoTypeName(), jugador.getMoneda().getMonedaName()).doubleValue();
				historicoApuestas.setCantidadMultiplier(cantidadMultiplier);
				double premioMultiplier = MathUtil.getPremioMultiplier(jugador, sorteo.getSorteoType().getSorteoTypeName()).doubleValue();
				historicoApuestas.setPremioMultiplier(premioMultiplier);
				historicoApuestas.setMoneda(jugador.getMoneda().getMonedaName().toString());
                historicoApuestaRepository.save(historicoApuestas);
                apuestaRepository.delete(apuesta);
            });
            if(sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA))
                Util.deleteAndCreateSorteoDiaria(estadoRepository,sorteoRepository,sorteoTypeRepository,SorteoTypeName.DIARIA, sorteoDiariaRepository, sorteoDiariaRepository.getSorteoDiariaById(id));
            else
                Util.deleteAndCreateSorteoDiaria(estadoRepository,sorteoRepository,sorteoTypeRepository,SorteoTypeName.CHICA, sorteoDiariaRepository, sorteoDiariaRepository.getSorteoDiariaById(id));
        }
        numeroGanadorRepository.save(numeroGanador);
        sorteoRepository.save(sorteo);
        return ResponseEntity.ok("Numero fijado");
    }

    @PostMapping("/jugador/apuestas/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApuestaActivaResponse getApuestasActivas(@Valid @RequestBody ObjectNode json,
                                                    @PathVariable Long id) {
        return getApuestaActivaResponse(json, id, userRepository, sorteoDiariaRepository,
                apuestaRepository, asistenteRepository, sorteoRepository);
    }

    @PostMapping("/jugador/apuestas/activa/{id}/detalles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<ApuestaActivaDetallesResponse> getDetallesApuestaActiva(@PathVariable Long id,
                                                                        @Valid @RequestBody ObjectNode json) {
        List<ApuestaActivaDetallesResponse> apuestasDetails = new ArrayList<>();
        User user = getUserFromJsonNode(json);
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Sorteo sorteo=sorteoRepository.getSorteoById(id);
        List<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUserOrderByNumeroAsc(sorteoDiaria, user);
        List<PairNV> pairNVList = new ArrayList<>();
        double total = 0;
        Jugador jugador=null;
        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        for (Apuesta apuesta : apuestas) {
            double cantidad = apuesta.getCantidad();
            if(sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                if(jugador.getCostoMil()!=0){
                    cantidad *= jugador.getCostoMil();
                }
            }else{
                if(jugador.getCostoChicaMiles()!=0){
                    cantidad *= jugador.getCostoChicaMiles();
                }else if(jugador.getCostoChicaPedazos()!=0){
                    cantidad *= jugador.getCostoChicaPedazos();
                }
            }
            total += cantidad;
            pairNVList.add(new PairNV(apuesta.getNumero(), cantidad));
        }
        ApuestaActivaDetallesResponse detallesResponse = new ApuestaActivaDetallesResponse();
        detallesResponse.setApuestas(pairNVList);
        detallesResponse.setTotal(total);
        detallesResponse.setTitle("Apuestas propias de - " + getUsernameStringFromObjectNode(json));
        detallesResponse.setMoneda(jugador.getMoneda().getMonedaName().toString());
        apuestasDetails.add(detallesResponse);
        List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
        asistentes.forEach(asistente -> {
            Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
            if (apuestaList.size() > 0) {
                ApuestaActivaDetallesResponse detallesResponse1 = new ApuestaActivaDetallesResponse();
                List<PairNV> pairNVList1 = new ArrayList<>();
                double total1 = 0;
                for (Apuesta apuesta : apuestaList) {
                    double cantidad = apuesta.getCantidad();
                    if(sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                        if(asistente.getJugador().getCostoMil()!=0){
                            cantidad *= asistente.getJugador().getCostoMil();
                        }
                    }else{
                        if(asistente.getJugador().getCostoChicaMiles()!=0){
                            cantidad *= asistente.getJugador().getCostoChicaMiles();
                        }else if(asistente.getJugador().getCostoChicaPedazos()!=0){
                            cantidad *= asistente.getJugador().getCostoChicaPedazos();
                        }
                    }
                    total1 += cantidad;
                    pairNVList1.add(new PairNV(apuesta.getNumero(), cantidad));
                }
                detallesResponse1.setApuestas(pairNVList1);
                detallesResponse1.setTotal(total1);
                detallesResponse1.setTitle("Apuestas de " + asistente.getUsername());
                apuestasDetails.add(detallesResponse1);
            }
        });
        return apuestasDetails;
    }


    @PostMapping("/jugador/apuestas/activas/{id}/detalles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<ApuestaActivaDetallesResponse> getDetallesByApuestaActivaId(@PathVariable Long id,
                                                                        @Valid @RequestBody ObjectNode json) {
        List<ApuestaActivaDetallesResponse> apuestasDetails = new ArrayList<>();
        User user = getUserFromJsonNode(json);
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Sorteo sorteo=sorteoRepository.getSorteoById(id);
        List<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUserOrderByNumeroAsc(sorteoDiaria, user);
        List<PairNV> pairNVList = new ArrayList<>();
        double total = 0;
        Jugador jugador=null;
        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        for (Apuesta apuesta : apuestas) {
            double cantidad = apuesta.getCantidad();
            if(sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                if(jugador.getCostoMil()!=0){
                    cantidad *= jugador.getCostoMil();
                }
            }else{
                if(jugador.getCostoChicaMiles()!=0){
                    cantidad *= jugador.getCostoChicaMiles();
                }else if(jugador.getCostoChicaPedazos()!=0){
                    cantidad *= jugador.getCostoChicaPedazos();
                }
            }
            total += cantidad;
            pairNVList.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
        }
        ApuestaActivaDetallesResponse detallesResponse = new ApuestaActivaDetallesResponse();
        detallesResponse.setApuestas(pairNVList);
        detallesResponse.setTotal(total);
        detallesResponse.setTitle("Apuestas propias de - " + getUsernameStringFromObjectNode(json));
        detallesResponse.setMoneda(jugador.getMoneda().getMonedaName().toString());
        apuestasDetails.add(detallesResponse);
        List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
        asistentes.forEach(asistente -> {
            Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
            if (apuestaList.size() > 0) {
                ApuestaActivaDetallesResponse detallesResponse1 = new ApuestaActivaDetallesResponse();
                List<PairNV> pairNVList1 = new ArrayList<>();
                double total1 = 0;
                for (Apuesta apuesta : apuestaList) {
                    total1 += apuesta.getCantidad();
                    pairNVList1.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
                }
                detallesResponse1.setApuestas(pairNVList1);
                detallesResponse1.setTotal(total1);
                detallesResponse1.setTitle("Apuestas de " + asistente.getUsername());
                apuestasDetails.add(detallesResponse1);
            }
        });
        return apuestasDetails;
    }


    // this.getAllJugadores => [/jugadores/list]
    @Deprecated
    @GetMapping("/jugador/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<JugadorResponse> getAllJugador() {
        List<JugadorResponse> jugadorResponses = new ArrayList<>();
        List<Jugador> jugadores = jugadorRepository.findAllByOrderByIdAsc();
        Iterable<SorteoDiaria> sorteoDiarias = sorteoDiariaRepository.findAll();
        jugadores.forEach(jugador -> {
            double[] total = {0.0};
            double[] comision = {0.0};
            sorteoDiarias.forEach(sorteoDiaria -> {
                Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, jugador);
                apuestas.forEach(apuesta -> {
                    double cantidad = apuesta.getCantidad();
                    if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                        if(jugador.getCostoMil()!=0){
                            cantidad *= jugador.getCostoMil();
                        }
                    }else{
                        if(jugador.getCostoChicaMiles()!=0){
                            cantidad *= jugador.getCostoChicaMiles();
                        }else if(jugador.getCostoChicaPedazos()!=0){
                            cantidad *= jugador.getCostoChicaPedazos();
                        }
                    }
                    total[0] += cantidad;
                    comision[0] += apuesta.getComision();
                });
            });
            JugadorResponse jugadorResponse = new JugadorResponse();
            jugadorResponse.setComision(comision[0]);
            jugadorResponse.setTotal(total[0]);
            jugadorResponse.setRiesgo(total[0] - comision[0]);
            jugadorResponse.setMonedaType(jugador.getMoneda().toString());
            jugadorResponse.setId(jugador.getId());
            jugadorResponse.setBalance(jugador.getBalance());
            jugadorResponse.setUsername(jugador.getUsername());
            jugadorResponse.setName(jugador.getName());
            List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
            if (asistentes.size() > 0) {
                List<AsistenteResponse> asistenteResponses = new ArrayList<>();
                asistentes.forEach(asistente -> {
                    AsistenteResponse asistenteResponse = new AsistenteResponse();
                    asistenteResponse.setName(asistente.getName());
                    asistenteResponse.setUsername(asistente.getUsername());
                    asistenteResponse.setId(asistente.getId());
                    asistenteResponses.add(asistenteResponse);
                });
                jugadorResponse.setAsistentes(asistenteResponses);
            }
            jugadorResponses.add(jugadorResponse);
        });
        return jugadorResponses;
    }
    
    @GetMapping("/jugadores/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<JugadorResponse> getAllJugadores() {
        return adminService.getAllJugadores();
    }


    @PostMapping("/jugador/balance/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public BalanceResponse getBalanceByJugadorId(@PathVariable Long id) {
        BalanceResponse balanceResponse = new BalanceResponse();
        List<PairSV> pairSVS = new ArrayList<>();
        User user = userRepository.getById(id);
        List<Sorteo> sorteos = sorteoRepository.findAllByStatus(new Status(StatusName.CURRENT));
        sorteos.forEach(sorteo -> {
            Resultado resultado = resultadoRepository.getBySorteoAndUser(sorteo, user);
            if (resultado != null) {
                double balance = resultado.getPremio() + resultado.getComision() - resultado.getCantApuesta();
                pairSVS.add(new PairSV(Util.formatLocalDate2StringShort(sorteo.getSorteoTime()), balance));
            }
        });
        balanceResponse.setTitle(Util.getMondayAndSundayDay());
        balanceResponse.setPairSVList(pairSVS);
        return balanceResponse;
    }

    //Reemplazado por SorteoService.findTodaySorteobyUsername [/activasResumen/judadores/{username}]
    @Deprecated
    @PostMapping("/jugador/apuestas/hoy/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public JugadorSorteosResponse findTodaySorteobyUsername(@Valid @RequestBody ObjectNode jsonNodes) {
        JugadorSorteosResponse jugadorSorteosResponse = new JugadorSorteosResponse();
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(jsonNodes.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        jugadorSorteosResponse.setName(user.getName());
        jugadorSorteosResponse.setMoneda(((Jugador) user).getMoneda().getMonedaName().toString());
        jugadorSorteosResponse.setSorteos(getSorteoResponses(jsonNodes, userRepository, sorteoDiariaRepository,
                apuestaRepository, asistenteRepository, sorteoRepository, jugadorRepository));
        return jugadorSorteosResponse;
    }

    //Reemplazado por SorteoController.getApuestasActivas [/sorteos/activos/{moneda}
    @Deprecated
    @GetMapping("/apuestas/activas/{moneda}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public List<ApuestasActivasResponse> getApuestasActivas(@PathVariable String moneda) {
        List<ApuestasActivasResponse> apuestasActivasResponses = new ArrayList<>();
        List<SorteoDiaria> sorteoDiarias = sorteoService.getActiveSorteosList();
        
        sorteoDiarias.forEach(sorteoDiaria -> {
            ApuestasActivasResponse activasResponse = new ApuestasActivasResponse();
            Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
            final double[] total = {0.0};
            final double[] comision = {0.0};
            final double[] premio = {0.0};

            if (sorteo.getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
                Optional<NumeroGanador> numeroGanadorOption=numeroGanadorRepository.getNumeroGanadorBySorteo(sorteo);
                numeroGanadorOption.ifPresent(numeroGanador -> activasResponse.setNumeroGanador(numeroGanador.getNumeroGanador().toString()));

                List<Resultado> resultados = resultadoRepository
                        .findAllBySorteo(sorteo);
                resultados.forEach(resultado -> {
                    double cambio=1;
                    if(moneda.equals("lempira") && ((Jugador) resultado.getUser()).getMoneda().getMonedaName().equals(MonedaName.DOLAR)){
                        cambio = resultado.getCambio().getCambio();
                    }else if(moneda.equals("dolar") && ((Jugador) resultado.getUser()).getMoneda().getMonedaName().equals(MonedaName.LEMPIRA)){
                        cambio = 1/resultado.getCambio().getCambio();
                    }
                    total[0] += resultado.getCantApuesta() * cambio;
                    comision[0] += resultado.getComision() * cambio;
                    premio[0] += resultado.getPremio() * cambio;
                });
            } else {
                Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);

                apuestas.forEach(apuesta -> {
                    double cambio=1;

                    Jugador jugador = null;
                    if(apuesta.getUser() instanceof Jugador){
                        jugador =  (Jugador) apuesta.getUser();
                        if(moneda.equalsIgnoreCase("lempira") && ((Jugador) apuesta.getUser()).getMoneda().getMonedaName().equals(MonedaName.DOLAR)){
                            cambio = apuesta.getCambio().getCambio();
                        }else if(moneda.equalsIgnoreCase("dolar") && ((Jugador) apuesta.getUser()).getMoneda().getMonedaName().equals(MonedaName.LEMPIRA)){
                            cambio = 1/apuesta.getCambio().getCambio();
                        }
                    }else if(apuesta.getUser() instanceof Asistente){
                        jugador =  ((Asistente) apuesta.getUser()).getJugador();
                        if(moneda.equals("lempira") && ((Asistente) apuesta.getUser()).getJugador().getMoneda().getMonedaName().equals(MonedaName.DOLAR)){
                            cambio = apuesta.getCambio().getCambio();
                        }else if(moneda.equals("dolar") && ((Asistente) apuesta.getUser()).getJugador().getMoneda().getMonedaName().equals(MonedaName.LEMPIRA)){
                            cambio = 1/apuesta.getCambio().getCambio();
                        }
                    }
                    double costoMilChica = 1;
                    double costoMilDiaria = 1;
                    double costoPedazoChica = 1;
                    if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                        costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 ;

                    }else{
                        costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1 ;
                        costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1 ;
                    }
                    total[0] += apuesta.getCantidad() *  cambio * costoMilChica * costoMilDiaria * costoPedazoChica;
                    comision[0] += apuesta.getComision() * cambio;
                });
            }

            double neta = total[0] - comision[0];
            activasResponse.setTotal(total[0]);
            activasResponse.setComision(comision[0]);
            activasResponse.setNeta(neta);
            activasResponse.setPremio(premio[0]);
            activasResponse.setBalance(neta - premio[0]);
            activasResponse.setId(sorteo.getId());
            activasResponse.setTitle(Util.formatTimestamp2String(sorteo.getSorteoTime()));
            activasResponse.setEstado(sorteo.getEstado().getEstado().toString());
            activasResponse.setType(sorteo.getSorteoType().getSorteoTypeName().toString());
            apuestasActivasResponses.add(activasResponse);
        });
        if(apuestasActivasResponses.get(0).getType().equalsIgnoreCase("chica")){
            apuestasActivasResponses.add(apuestasActivasResponses.get(0));
            apuestasActivasResponses.remove(0);
        }
        return apuestasActivasResponses;
    }

    //Reemplazado por SorteoController.getDetalleApuestasBySorteo [/activos/detalles/{id}/{moneda}]
    @Deprecated
    @PostMapping("/apuestas/activas/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ApuestaActivaResumenResponse getDetallesApuestasActivasById(@PathVariable Long id,
                                                                       @Valid @RequestBody ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String type = mapper.convertValue(json.get("type"), String.class);

        List<TuplaRiesgo> tuplaRiesgos = new ArrayList<>();
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Sorteo sorteo = sorteoRepository.getSorteoById(id);
        Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
        double[] cantidad = new double[100];
        double[] riesgo = new double[100];
        final double[] total = {0.0};
        final double[] comision = {0.0};

        apuestaList.forEach(apuesta -> {
            int numero = apuesta.getNumero();
            User user = apuesta.getUser();
            if (user instanceof Jugador) {
                Jugador jugador = (Jugador) user;
                calcularCantRiesgo(sorteo, cantidad, riesgo, apuesta, numero, jugador, total, type, comision);
            } else if (user instanceof Asistente) {
                Jugador jugador = ((Asistente) user).getJugador();
                calcularCantRiesgo(sorteo, cantidad, riesgo, apuesta, numero, jugador, total, type, comision);
            }
        });
        double max = 0.0;
        int pos = -1;
        double totalValue = 0;
        for (int i = 0; i < 100; i++) {
            if (cantidad[i] != 0) {
                TuplaRiesgo tuplaRiesgo = new TuplaRiesgo();
                tuplaRiesgo.setNumero(i);
                tuplaRiesgo.setDineroApostado(cantidad[i]);
                tuplaRiesgo.setTotalRiesgo(riesgo[i]);
                tuplaRiesgos.add(tuplaRiesgo);
                totalValue += cantidad[i];
                if (max < riesgo[i]) {
                    max = riesgo[i];
                    pos = i;
                }
            }
        }

        TuplaRiesgo tuplaRiesgo = new TuplaRiesgo();
        if (pos != -1) {
            tuplaRiesgo.setNumero(pos);
            tuplaRiesgo.setDineroApostado(cantidad[pos]);
            tuplaRiesgo.setTotalRiesgo(riesgo[pos]);
        }

        return new ApuestaActivaResumenResponse(tuplaRiesgo, tuplaRiesgos, comision[0], totalValue);
    }

    //Replaced by SorteoController.bloquearSorteo [/sorteo/bloquear/{id}]
    @Deprecated
    @PutMapping("/apuesta/bloquear/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MASTER')")
    public ResponseEntity<?> bloquearApuesta(@PathVariable Long id, @Valid @RequestBody ObjectNode json) {
        Sorteo sorteo = sorteoRepository.getSorteoById(id);
        sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
        sorteoRepository.save(sorteo);
        return ResponseEntity.ok("Apuesta bloqueada");
    }

    private void calcularCantRiesgo(Sorteo sorteo, double[] cantidad, double[] riesgo,
                                    Apuesta apuesta, int numero, Jugador jugador, double[] total,
                                    String type, double[] comision) {
        Double cambio = 1.0;
        double costoMilChica = 1;
        double costoMilDiaria = 1;
        double costoPedazoChica = 1;
        if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.DOLAR))
                && type.equals("lempira")) {
            cambio = apuesta.getCambio().getCambio();
        } else if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.LEMPIRA))
                && type.equals("dolar")) {
            cambio = 1 / apuesta.getCambio().getCambio();
        }
        double premio;
        if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
            if (jugador.getTipoApostador().getApostadorName().equals(ApostadorName.DIRECTO)) {
                premio = jugador.getPremioDirecto() * apuesta.getCantidad() * cambio;
            } else {
                premio = jugador.getPremioMil() * apuesta.getCantidad() * cambio;
            }
            costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 ;

            cantidad[numero] += apuesta.getCantidad() * cambio * costoMilDiaria;
            comision[0] += apuesta.getComision() * cambio;
            riesgo[numero] += premio;
        } else {
            if (jugador.getTipoChica().getChicaName().equals(ChicaName.DIRECTO)) {
                premio = jugador.getPremioChicaDirecto() * apuesta.getCantidad() * cambio;
            } else if (jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)) {
                premio = jugador.getPremioChicaMiles() * 1000 * apuesta.getCantidad() * cambio;
            } else {
                premio = jugador.getPremioChicaPedazos() * apuesta.getCantidad() * cambio;
            }
            costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1 ;
            costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1 ;

            cantidad[numero] += apuesta.getCantidad() * cambio * costoMilChica * costoPedazoChica;
            comision[0] += apuesta.getComision() * cambio;
            riesgo[numero] += premio;
        }
        total[0] += premio;
    }

    static List<SorteoResponse> getSorteoResponses(@RequestBody @Valid ObjectNode jsonNodes,
                                                   UserRepository userRepository,
                                                   SorteoDiariaRepository sorteoDiariaRepository,
                                                   ApuestaRepository apuestaRepository,
                                                   AsistenteRepository asistenteRepository,
                                                   SorteoRepository sorteoRepository,
                                                   JugadorRepository jugadorRepository) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(jsonNodes.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        List<SorteoResponse> sorteoResponses = new ArrayList<>();
        Jugador jugador = null;
        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        double total = 0;
        int i = 0;
        String moneda="LEMPIRA";
        Iterable<SorteoDiaria> sorteosDB = sorteoDiariaRepository.findAll();
        List<SorteoDiaria> sorteos = new ArrayList<SorteoDiaria>();
        sorteosDB.forEach(sorteos::add);
        sorteos.sort((sorteo1, sorteo2) -> sorteo1.getSorteoTime().compareTo(sorteo2.getSorteoTime()));
        
        for (SorteoDiaria sorteoDiaria : sorteos) {
            Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
            for (Apuesta apuesta : apuestas) {
                double cantidad = apuesta.getCantidad();
               
                if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                    if(jugador.getCostoMil()!=0){
                        cantidad *= jugador.getCostoMil();
                    }
                }else{
                    if(jugador.getCostoChicaMiles()!=0){
                        cantidad *= jugador.getCostoChicaMiles();
                    }else if(jugador.getCostoChicaPedazos()!=0){
                        cantidad *= jugador.getCostoChicaPedazos();
                    }
                }
                total += cantidad;
            }
            if (user instanceof Jugador) {
                List<Asistente> asistentes = asistenteRepository.findAllByJugador((Jugador) user);
                double cantidad = asistentes.stream().map(asistente ->
                        apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente))
                        .filter(apuestaList -> apuestaList.size() > 0)
                        .flatMap(Collection::stream)
                        .mapToDouble(Apuesta::getCantidad)
                        .sum();;
                if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                    if(jugador.getCostoMil()!=0){
                        cantidad *= jugador.getCostoMil();
                    }
                }else{
                    if(jugador.getCostoChicaMiles()!=0){
                        cantidad *= jugador.getCostoChicaMiles();
                    }else if(jugador.getCostoChicaPedazos()!=0){
                        cantidad *= jugador.getCostoChicaPedazos();
                    }
                }
                total += cantidad;

                moneda= ((Jugador) user).getMoneda().getMonedaName().toString();
            }else{
                moneda= ((Asistente) user).getJugador().getMoneda().getMonedaName().toString();
            }

            Optional<Sorteo> sorteo = sorteoRepository.findById(sorteoDiaria.getId());
            String estado = "ABIERTA";
            if (sorteo.isPresent()) {
                estado = sorteo.get().getEstado().getEstado().toString();
            }
            double comision = 0.0;
            LocalDate localDate = sorteoDiaria.getSorteoTime().toLocalDateTime().toLocalDate();
//            if (user instanceof Jugador) {
//                comision = jugadorRepository.getById(user.getId()).getComisionDirecto();
//            }
//            comision = 1;
            if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                comision = jugador.getComisionDirecto();
            }else{
                comision = (jugador.getComisionChicaDirecto() + jugador.getComisionChicaPedazos());
            }
            comision = total * comision / 100;
            sorteoResponses.add(new SorteoResponse(sorteoDiaria.getId(),
                    Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()),
                    Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()),
                    Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()),
                    total, comision, total - comision, estado, moneda, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString()));
            total = 0;
        }
        return sorteoResponses;
    }

    static ApuestaActivaResponse getApuestaActivaResponse(@RequestBody @Valid ObjectNode json,
                                                          @PathVariable Long id,
                                                          UserRepository userRepository,
                                                          SorteoDiariaRepository sorteoDiariaRepository,
                                                          ApuestaRepository apuestaRepository,
                                                          AsistenteRepository asistenteRepository,
                                                          SorteoRepository sorteoRepository) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(json.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Sorteo sorteo=sorteoRepository.getSorteoById(id);
        List<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUserOrderByNumeroAsc(sorteoDiaria, user);
        Jugador jugador = null;
        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        List<PairNV> pairNVList = new ArrayList<>();
        double [] total={0.0};
        // Apuestas asistente
        for (Apuesta apuesta : apuestas) {
            double cantidad = apuesta.getCantidad();
            if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
                if (jugador.getCostoMil() != 0) {
                    cantidad *= jugador.getCostoMil();
                }
            } else {
                if (jugador.getCostoChicaMiles() != 0) {
                    cantidad *= jugador.getCostoChicaMiles();
                } else if (jugador.getCostoChicaPedazos() != 0) {
                    cantidad *= jugador.getCostoChicaPedazos();
                }
            }
            total[0] += cantidad;
            pairNVList.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
        }
        if (user instanceof Jugador) {
            List<Asistente> asistentes = asistenteRepository.findAllByJugador(jugador);
            asistentes.forEach(asistente -> {
                Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
                if (apuestaList.size() > 0) {
                    for (Apuesta apuesta : apuestaList) {
                        double cantidad = apuesta.getCantidad();
                        if(sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
                            if(asistente.getJugador().getCostoMil()!=0){
                                cantidad *= asistente.getJugador().getCostoMil();
                            }
                        }else{
                            if(asistente.getJugador().getCostoChicaMiles()!=0){
                                cantidad *= asistente.getJugador().getCostoChicaMiles();
                            }else if(asistente.getJugador().getCostoChicaPedazos()!=0){
                                cantidad *= asistente.getJugador().getCostoChicaPedazos();
                            }
                        }

                        total[0] += cantidad ;
                        AtomicBoolean flag = new AtomicBoolean(false);
                        double finalCantidad = cantidad;
                        pairNVList.forEach(pairNV -> {
                            if (pairNV.getNumero().equals(apuesta.getNumero())) {
                                pairNV.setValor(pairNV.getValor() + apuesta.getCantidad());
                                flag.set(true);
                            }
                        });
                        if (!flag.get()) {
                            pairNVList.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
                        }
                    }
                }
            });
        }
        double comision = 0.0;
//        if (user instanceof Jugador) {
//            Sorteo sorteo = sorteoRepository.getSorteoById(id);
        if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.CHICA)) {
            // O una u otra nunca ambas
            comision = jugador.getComisionChicaDirecto() + jugador.getComisionChicaPedazos();
        } else {
            comision = jugador.getComisionDirecto();
        }
//        }else if(user instanceof Asistente){
//            if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.CHICA)) {
//                // O una u otra nunca ambas
//                comision = jugador.getComisionDirecto() + jugador.getComisionChicaPedazos();
//            } else {
//                comision = jugador.getComisionDirecto();
//            }
//        }
        Collections.sort(pairNVList);
        comision = total[0] * comision / 100;
        ApuestaActivaResponse apuestaActivaResponse = new ApuestaActivaResponse();
        apuestaActivaResponse.setList(pairNVList);
        apuestaActivaResponse.setTitle(Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()));
        apuestaActivaResponse.setTotal(total[0]);
        apuestaActivaResponse.setComision(comision);
        apuestaActivaResponse.setRiesgo(total[0] - comision);
        apuestaActivaResponse.setType(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString());
        return apuestaActivaResponse;
    }

    private void handleMapForUsers(Map<String, double[]> map, Apuesta apuesta,
                                   Jugador jugador, double[] aux, int pos, Sorteo sorteo) {
        String username= jugador.getUsername();
        double cantidad = apuesta.getCantidad();
        if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
            if(jugador.getCostoMil() != 0){
                cantidad *= jugador.getCostoMil();
            }
        }else{
            if(jugador.getCostoChicaMiles() != 0){
                cantidad *= jugador.getCostoMil();
            }else if(jugador.getCostoChicaPedazos() != 0){
                cantidad *= jugador.getCostoChicaPedazos();
            }
        }

        if (map.containsKey(username)) {
            aux = map.get(username);
            aux[pos] += cantidad;
            map.replace(username, aux);
        } else {
            aux[pos] = cantidad;
            map.put(username, aux);
        }
    }


    private void resultadosFillData(List<PairJB> pairJBS,
                                    double[] total,
                                    double[] comision,
                                    double[] premio,
                                    List<Resultado> resultados,
                                    String type

                                    ) {
        resultados.forEach(resultado -> {

            Jugador jugador = (Jugador) resultado.getUser();
            double cambio = 1;
            if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.DOLAR))
                    && type.equals("lempira")) {
                cambio = cambioRepository.getByCambio(resultado.getCambio()).getCambio();
            } else if (jugador.getMoneda().equals(monedaRepository.findByMonedaName(MonedaName.LEMPIRA))
                    && type.equals("dolar")) {
                cambio = 1 / cambioRepository.getByCambio(resultado.getCambio()).getCambio();
            }
            total[0] += resultado.getCantApuesta() * cambio;
            comision[0] += resultado.getComision() * cambio;
            premio[0] += resultado.getPremio() * cambio;
            double balanceJugador = resultado.getPremio() + resultado.getComision()
                    - resultado.getCantApuesta();
            pairJBS.add(new PairJB(jugador.getId(), jugador.getUsername(), balanceJugador, jugador.getMoneda().getMonedaName().name()));
        });
    }

    static void setRoles(Set<String> strRoles, Set<Role> roles, RoleRepository roleRepository) {
        strRoles.forEach(role -> {
            switch (role) {
                case "admin":
                    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException(
                                    "Fail! -> Cause: User Role not find."));
                    roles.add(adminRole);
                    break;
                case "master":
                    Role masterRole = roleRepository.findByName(RoleName.ROLE_MASTER)
                            .orElseThrow(() -> new RuntimeException(
                                    "Fail! -> Cause: Master Role not find."));
                    roles.add(masterRole);

                    break;
                case "supervisor":
                    Role supervisorRole = roleRepository.findByName(RoleName.ROLE_SUPERVISOR)
                            .orElseThrow(() -> new RuntimeException(
                                    "Fail! -> Cause: User Role not find."));
                    roles.add(supervisorRole);

                    break;
                case "asistente":
                    Role asisRole = roleRepository.findByName(RoleName.ROLE_ASIS)
                            .orElseThrow(() -> new RuntimeException(
                                    "Fail! -> Cause: User Role not find."));
                    roles.add(asisRole);
                    break;
                default:
                    Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                            .orElseThrow(() -> new RuntimeException(
                                    "Fail! -> Cause: User Role not find."));
                    roles.add(userRole);
            }
        });
    }

    private User getUserFromJsonNode(ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(json.get("username"), String.class);
        return userRepository.getByUsername(username);
    }

    private String getUsernameStringFromObjectNode(ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(json.get("username"), String.class);
        return username;
    }

    private SorteoDiaria getThisWeekChica() {
        Iterable<SorteoDiaria> sorteoDiarias = sorteoDiariaRepository.findAll();
        for (SorteoDiaria sorteoDiaria :
                sorteoDiarias) {
            if (sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.CHICA))
                return sorteoDiaria;
        }

        return null;
    }
}
