package com.devteam.fantasy.controller;


import com.devteam.fantasy.message.request.LoginForm;
import com.devteam.fantasy.message.request.UpdateNumberForm;
import com.devteam.fantasy.message.response.*;
import com.devteam.fantasy.model.*;
import com.devteam.fantasy.repository.*;
import com.devteam.fantasy.service.SorteoService;
import com.devteam.fantasy.service.SorteoServiceImpl;
import com.devteam.fantasy.service.UserService;
import com.devteam.fantasy.util.EstadoName;
import com.devteam.fantasy.util.MonedaName;
import com.devteam.fantasy.util.PairNV;
import com.devteam.fantasy.util.SorteoTypeName;
import com.devteam.fantasy.util.Util;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.catalina.mapper.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.swing.SortingFocusTraversalPolicy;
import javax.validation.Valid;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.devteam.fantasy.controller.AdminController.getSorteoResponses;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/user")
public class PlayerController {
    @Autowired
    PasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SorteoRepository sorteoRepository;

    @Autowired
    SorteoDiariaRepository sorteoDiariaRepository;

    @Autowired
    RestriccionRepository restriccionRepository;

    @Autowired
    ApuestaRepository apuestaRepository;

    @Autowired
    EstadoRepository estadoRepository;

    @Autowired
    AsistenteRepository asistenteRepository;

    @Autowired
    JugadorRepository jugadorRepository;

    @Autowired
    CambioRepository cambioRepository;

    @Autowired
    HistoricoApuestaRepository historicoApuestaRepository;

    @Autowired
    ResultadoRepository resultadoRepository;

    @Autowired
    NumeroGanadorRepository numeroGanadorRepository;

    @Autowired
    SorteoService sorteoService;

    @Autowired
	private UserService userService;
    
    
    @PostMapping("/password/update")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public ResponseEntity<?> updatePasswword(@Valid @RequestBody LoginForm loginForm) {
        if (loginForm.getUsername().charAt(0) == 'C') {
            return new ResponseEntity<String>("Fail -> Username is not valid!",
                    HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.findByUsername(loginForm.getUsername())
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with -> username : " + loginForm.getUsername())
                );
        user.setPassword(encoder.encode(loginForm.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(new SimpleResponse("Password update"));
    }

    @PostMapping("/jugador/comision")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public CostoMilAndComisionResponse getJugadorComisionDirecto(@Valid @RequestBody ObjectNode json) {
        CostoMilAndComisionResponse comisionResponse=new CostoMilAndComisionResponse();
        User user = getUserFromJsonNode(json);
        ObjectMapper mapper = new ObjectMapper();
        double comisionValue=0.0;
        double costoMil=1;
        String comision = mapper.convertValue(json.get("type"), String.class);
        Jugador jugador = new Jugador();
        if(user instanceof Jugador){
            jugador= jugadorRepository.getById(user.getId());
        }else if(user instanceof Asistente){
            jugador = ((Asistente) user).getJugador();
        }
        if (comision.equals("chica")) {
                jugador= jugadorRepository.getById(user.getId());
                comisionValue = jugador.getComisionChicaDirecto() +
                        jugador.getComisionChicaPedazos() * jugador.getCostoChicaPedazos();
                costoMil = jugador.getCostoChicaMiles() == 0 ? 1 : jugador.getCostoChicaMiles();
                costoMil = jugador.getCostoChicaPedazos() == 0 ? costoMil : jugador.getCostoChicaPedazos();
        }else{
            comisionValue=jugadorRepository.getById(user.getId()).getComisionDirecto();
            costoMil = jugador.getCostoMil() == 0 ? 1 : jugador.getCostoMil();
        }
        comisionResponse.setComision(comisionValue);
        comisionResponse.setCostoMil(costoMil);
//        return jugadorRepository.getById(user.getId()).getComisionDirecto();
        return comisionResponse;
    }

    @GetMapping("/apuestas/hoy")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public List<SorteoResponse> findTodaySorteo() {
        List<SorteoResponse> sorteoResponses = new ArrayList<>();
        double total = 0;
        int i = 0;
        Iterable<SorteoDiaria> sorteosDB = sorteoDiariaRepository.findAll();
        
        List<SorteoDiaria> sorteos = new ArrayList<SorteoDiaria>();
        sorteosDB.forEach(sorteos::add);
        sorteos.sort((sorteo1, sorteo2) -> sorteo1.getSorteoTime().compareTo(sorteo2.getSorteoTime()));
        
        for (SorteoDiaria sorteoDiaria : sorteos) {
            for (Apuesta apuesta :
                    sorteoDiaria.getApuestas()) {
                User user = apuesta.getUser();
                double cantidad=apuesta.getCantidad();
                Jugador jugador = null;
                if(user instanceof Jugador){
                    jugador = (Jugador) user;
                }else{
                    jugador = ((Asistente) user).getJugador();
                }
                double costoMilChica = 1;
                double costoMilDiaria = 1;
                double costoPedazoChica = 1;
                if (sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)) {
                    costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1;
                } else {
                    costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1;
                    costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1;
                }
                total += cantidad * costoMilChica * costoPedazoChica * costoMilDiaria;
            }
            Optional<Sorteo> sorteo = sorteoRepository.findById(sorteoDiaria.getId());
            String estado = "ABIERTA";
            if (sorteo.isPresent()) {
                estado = sorteo.get().getEstado().getEstado().toString();
            }
            LocalDate localDate = sorteoDiaria.getSorteoTime().toLocalDateTime().toLocalDate();
            sorteoResponses.add(new SorteoResponse(sorteoDiaria.getId(),
                    Util.formatLocalDatetoString(localDate, i++), total, 0.0, 0.0, estado));
            total = 0;

        }
        return sorteoResponses;
    }

    //SorteoController.getSorteosActivosByUsername => [/activos/judadores/{username}]
    @Deprecated
    @PostMapping("/apuestas/hoy/list")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public List<SorteoResponse> findTodaySorteobyUsername(@Valid @RequestBody ObjectNode jsonNodes) throws Exception {
    	ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(jsonNodes.get("username"), String.class);
        User user = userRepository.getByUsername(username);
    	
    	List<SorteoDiaria> sorteos = sorteoService.getActiveSorteosList(user);
    	List<SorteoResponse> sorteosResponses = sorteoService.getSorteosResponses(sorteos, user);
    	
    	return sorteosResponses;
    }

    @PostMapping("/apuestas/asistente/hoy/list")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public List<SorteoResponse> findTodaySorteosbyUsernameAsistente(@Valid @RequestBody ObjectNode jsonNodes) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(jsonNodes.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        List<SorteoResponse> sorteoResponses = new ArrayList<>();
        Asistente asistente = ((Asistente) user);
        double total = 0;
        int i = 0;
        String moneda="LEMPIRA";
        Iterable<SorteoDiaria> sorteosDB = sorteoDiariaRepository.findAll();
        
        List<SorteoDiaria> sorteos = new ArrayList<SorteoDiaria>();
        sorteosDB.forEach(sorteos::add);
        sorteos.sort((sorteo1, sorteo2) -> sorteo1.getSorteoTime().compareTo(sorteo2.getSorteoTime()));
        
        for (SorteoDiaria sorteoDiaria : sorteos) {
            Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
            for (Apuesta apuesta :
                    apuestas) {
                total += apuesta.getCantidad();
            }

            Optional<Sorteo> sorteo = sorteoRepository.findById(sorteoDiaria.getId());
            String estado = "ABIERTA";
            if (sorteo.isPresent()) {
                estado = sorteo.get().getEstado().getEstado().toString();
            }
            moneda= asistente.getJugador().getMoneda().getMonedaName().toString();

            sorteoResponses.add(new SorteoResponse(sorteoDiaria.getId(),
                    Util.formatTimestamp2String(sorteoDiaria.getSorteoTime()),
                    Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()),
                    Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()),
                    total, 0.0, 0.0, estado, moneda, sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString()));
            total = 0;
        }
        return sorteoResponses;
    }

    @PostMapping("/apuestas/activa/{id}/detalles")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
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
//            pairNVList.add(new PairNV(apuesta.getNumero(), cantidad));
            pairNVList.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
        }
        ApuestaActivaDetallesResponse detallesResponse = new ApuestaActivaDetallesResponse();
        detallesResponse.setApuestas(pairNVList);
        detallesResponse.setTotal(total);
        detallesResponse.setTitle(jugador.getUsername() +" - "+Util.getMonedaSymbolFromMonedaName(jugador.getMoneda().getMonedaName())+" ["+jugador.getName()+"]");
        detallesResponse.setUserId(jugador.getId());
        apuestasDetails.add(detallesResponse);
        List<Asistente> asistentes = asistenteRepository.findAllByJugadorAndUserState(jugador, UserState.ACTIVE);
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
//                    pairNVList1.add(new PairNV(apuesta.getNumero(), cantidad));
                    pairNVList1.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));

                }
                Collections.sort(pairNVList1);
                detallesResponse1.setApuestas(pairNVList1);
                detallesResponse1.setTotal(total1);
                detallesResponse1.setTitle(asistente.getUsername()+" ["+asistente.getName()+"]");
                detallesResponse1.setUserId(asistente.getId());
                apuestasDetails.add(detallesResponse1);
            }
        });
        return apuestasDetails;
    }

    @GetMapping("/apuestas/numeros")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public List<NumeroPlayerEntryResponse> getListApuestaNumbers() {
        List<NumeroPlayerEntryResponse> topes = new ArrayList<>();
        List<Restriccion> restriciones = restriccionRepository.findAllByTimestamp(Util.getTodayTimeStamp());
        generateTopes(topes, restriciones);
        return topes;
    }

    @PostMapping("/apuestas/{id}/numeros")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public ApuestaNumeroPlayerEntryResponse getListNumberById(@PathVariable Long id,
                                                             @Valid @RequestBody ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(json.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        ApuestaNumeroPlayerEntryResponse playerEntryResponse=new ApuestaNumeroPlayerEntryResponse();
        Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
        List<NumeroPlayerEntryResponse> topes = new ArrayList<>();
        List<Restriccion> restriciones = restriccionRepository.findAllByTimestamp(Util.getTodayTimeStamp());
        generateTopes(topes, restriciones);

        Jugador jugador = null;
        double cambio = 1;

        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        double costoMilDiaria = 1 ;
        double costoMilChica = 1 ;
        double costoPedazoChica = 1 ;
        if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
            costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 ;
        }else{
            costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1 ;
            costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1 ;
        }
        double finalCostoMilDiaria = costoMilDiaria;
        double finalCostoMilChica = costoMilChica;
        double finalCostoPedazoChica = costoPedazoChica;
        topes.forEach(entry -> apuestas.forEach(apuesta -> {
            if (Integer.valueOf(entry.getNumero()).equals(apuesta.getNumero())) {
                entry.setCurrent(apuesta.getCantidad() * finalCostoMilDiaria * finalCostoMilChica * finalCostoPedazoChica);
            }
        }));
        playerEntryResponse.setName(Util.formatTimestamp2StringApuestas(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString(), sorteoDiaria.getSorteoTime()));
        playerEntryResponse.setList(topes);
        return playerEntryResponse;
    }

    @PostMapping("/apuestas/{id}/numeros/list")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public ApuestaNumeroPlayerEntryResponse getListNumberByApuestaId(@PathVariable Long id,
                                                              @Valid @RequestBody ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(json.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        ApuestaNumeroPlayerEntryResponse playerEntryResponse=new ApuestaNumeroPlayerEntryResponse();

        List<NumeroPlayerEntryResponse> topes = new ArrayList<>();
        List<Restriccion> restriciones = restriccionRepository.findAllByTimestamp(Util.getTodayTimeStamp());
        generateTopes(topes, restriciones);
        Set<Apuesta> apuestas= apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);

        apuestas.forEach(apuesta -> {
            topes.forEach( data ->{
                if(Integer.valueOf(data.getNumero()).equals(apuesta.getNumero())){
                    data.setNoFirst(true);
                }
            });
        });

        playerEntryResponse.setName(Util.formatTimestamp2StringApuestas(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString(), sorteoDiaria.getSorteoTime()));
        playerEntryResponse.setList(topes);
        playerEntryResponse.setType(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString());
        playerEntryResponse.setDay(Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()));
        playerEntryResponse.setHour(Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()));
        
        return playerEntryResponse;
    }


    //Replace by SorteoController.submitApuestas [/sorteos/activos/{id}/apuestas]
    @Deprecated
    @PostMapping("/apuestas/{id}/numeros/update")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public ResponseEntity<?> updateNumerosApuestas(@Valid @RequestBody ObjectNode json, @PathVariable Long id) {
        JsonNode listElements = json.get("data");
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(json.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        Cambio cambio = cambioRepository.findFirstByOrderByIdDesc();
        List<NumeroPlayerEntryResponse> data = mapper.convertValue(
                listElements,
                new TypeReference<List<NumeroPlayerEntryResponse>>() {
                }
        );
        List<NumeroPlayerEntryResponse> result = data.stream()
                .filter(entry -> 0.0 != entry.getCurrent())
                .collect(Collectors.toList());
        double balance = 0.0;

        for (NumeroPlayerEntryResponse entryResponse : result) {
            SorteoDiaria sorteoDiaria1 = sorteoDiariaRepository.getSorteoDiariaById(id);
            double costoMil = 1;
            double comision = 0.0;
            double costoPedazo = 0.0;
            Sorteo sorteo = sorteoRepository.getSorteoById(id);

            if (user instanceof Jugador) {
//                costoMil = ((Jugador) user).getCostoMil();
                comision = ((Jugador) user).getComisionDirecto();
                if (sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.CHICA)) {
//                    costoMil = ((Jugador) user).getCostoChicaMiles() + ((Jugador) user).getCostoChicaPedazos();
                    comision = ((Jugador) user).getComisionChicaPedazos() + ((Jugador) user).getComisionChicaDirecto();
                    costoPedazo = ((Jugador) user).getCostoChicaPedazos();
                }
            } else if (user instanceof Asistente) {
                Jugador jugador = ((Asistente) user).getJugador();
//                costoMil = jugador.getCostoMil();
                comision = jugador.getComisionDirecto();
                if (sorteo.getSorteoType().equals(new SorteoType(SorteoTypeName.CHICA))) {
//                    costoMil = jugador.getCostoChicaMiles() + jugador.getCostoChicaPedazos();
                    comision = jugador.getComisionChicaPedazos() * entryResponse.getCurrent() + jugador.getComisionChicaDirecto();
                    costoPedazo = jugador.getCostoChicaPedazos();
                }
            }
            if (costoMil == 0) {
                costoMil = 1;
            }
            if (costoPedazo == 0) {
                costoPedazo = 1;
            }
            Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria1, user);
            Restriccion restriccion = restriccionRepository
                    .getByNumeroAndTimestamp(Integer.valueOf(entryResponse.getNumero()), Util.getTodayTimeStamp());
            if (restriccion != null) {
                int diff = restriccion.getPuntos() - restriccion.getPuntosCurrent();
                Apuesta apuesta_ele = apuestaList.stream()
                        .filter(apuesta -> Integer.valueOf(entryResponse.getNumero()).equals(apuesta.getNumero()))
                        .findAny()
                        .orElse(null);
                if (diff > 0 && apuesta_ele != null) {
                    double current = entryResponse.getCurrent();
                    updateDataApuestaAndRestriccion(restriccion, diff, apuesta_ele, current, comision, costoMil, costoPedazo);
                    apuesta_ele.setCambio(cambio);
                    balance -= apuesta_ele.getCantidad();
                    apuestaRepository.save(apuesta_ele);
                } else if (diff > 0) {
                    Apuesta apuesta = new Apuesta();
                    Optional<SorteoDiaria> sorteoDiaria = sorteoDiariaRepository.findById(id);
                    sorteoDiaria.ifPresent(apuesta::setSorteoDiaria);
                    apuesta.setNumero(Integer.valueOf(entryResponse.getNumero()));
                    double current = entryResponse.getCurrent();
                    updateDataApuestaAndRestriccion(restriccion, diff, apuesta, current, comision, costoMil, costoPedazo);
                    apuesta.setCambio(cambio);
                    Optional<User> users = userRepository.findByUsername(username);
                    users.ifPresent(apuesta::setUser);
                    balance -= apuesta.getCantidad();
                    apuesta.setDate(Timestamp.valueOf(LocalDateTime.now()));
                    apuestaRepository.save(apuesta);
                }
            } else {
                Apuesta apuesta_ele = apuestaList.stream()
                        .filter(apuesta -> Integer.valueOf(entryResponse.getNumero()).equals(apuesta.getNumero()))
                        .findAny()
                        .orElse(null);
                if (apuesta_ele != null) {
                    apuesta_ele.setCantidad(entryResponse.getCurrent());
                    apuesta_ele.setComision(entryResponse.getCurrent() * costoPedazo * comision / 100);
                    apuesta_ele.setCambio(cambio);
                    apuesta_ele.setDate(Timestamp.valueOf(LocalDateTime.now()));
                    balance -= apuesta_ele.getCantidad();
                    apuestaRepository.save(apuesta_ele);
                } else {
                    Apuesta apuesta = new Apuesta();
                    Optional<SorteoDiaria> sorteoDiaria = sorteoDiariaRepository.findById(id);
                    sorteoDiaria.ifPresent(apuesta::setSorteoDiaria);
                    apuesta.setNumero(Integer.valueOf(entryResponse.getNumero()));
                    apuesta.setCantidad(entryResponse.getCurrent());
                    apuesta.setComision(entryResponse.getCurrent() * costoPedazo *comision / 100);
                    apuesta.setCambio(cambio);
                    apuesta.setDate(Timestamp.valueOf(LocalDateTime.now()));
                    Optional<User> users = userRepository.findByUsername(username);
                    users.ifPresent(apuesta::setUser);
                    balance -= apuesta.getCantidad();
                    apuestaRepository.save(apuesta);
                }
            }
        }
        return ResponseEntity.ok().body("Update numeros");
    }

    //Replaced by SorteoController.getApuestasActivasBySorteoAndJugador [sorteos/activos/{id}/apuestas/{username}]
    @PostMapping("/apuestas/activas/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public ApuestaActivaResponse getApuestasActivas(@Valid @RequestBody ObjectNode json,
                                                    @PathVariable Long id) {
        return getApuestaActivaResponsePlayer(json, id, userRepository,
                sorteoDiariaRepository,
                apuestaRepository,
                asistenteRepository,
                sorteoRepository);
    }

    @PostMapping("/apuestas/activas/{id}/update")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public ResponseEntity<?> updateApuestasActivas(@PathVariable Long id,
                                                   @Valid @RequestBody ObjectNode json) {
        JsonNode listElements = json.get("data");
        ObjectMapper mapper = new ObjectMapper();
        Cambio cambio = cambioRepository.findFirstByOrderByIdDesc();
        List<PairNV> data = mapper.convertValue(
                listElements,
                new TypeReference<List<PairNV>>() {
                }
        );
        String username = mapper.convertValue(json.get("username"), String.class);
        User user = userRepository.getByUsername(username);
        Jugador jugador = null;

        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        double costoMilDiaria = 1 ;
        double costoMilChica = 1 ;
        double costoPedazoChica = 1 ;

        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Sorteo sorteo = sorteoRepository.getSorteoById(id);
        if(sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
            costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 ;
        }else{
            costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1 ;
            costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1 ;
        }

        Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
        for (Apuesta apuesta : apuestas) {
            PairNV updateNumberForm = data.stream()
                    .filter(entry -> apuesta.getNumero() == entry.getNumero())
                    .collect(Util.toSingleton());
            if (updateNumberForm.getValor() == 0) {
                apuestaRepository.delete(apuesta);
            } else {
                apuesta.setCantidad(updateNumberForm.getValor());
                apuesta.setCambio(cambio);
                apuesta.setDate(Timestamp.valueOf(LocalDateTime.now()));
                apuestaRepository.save(apuesta);
            }
        }
        return ResponseEntity.ok("Update number");
    }


    @PostMapping("/sorteos/activos/{id}/delete/number")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public ResponseEntity<?> deleteApuestaNumberByApuestaId(@PathVariable Long id,
                                                            @Valid @RequestBody ObjectNode json){

        ObjectMapper mapper = new ObjectMapper();
        Integer numero = mapper.convertValue(json.get("numero"), Integer.class);
        Long userId=mapper.convertValue(json.get("userId"), Long.class);
       
        Apuesta apuesta= apuestaRepository.getApuestaByNumeroAndSorteoDiariaAndUser(numero, sorteoDiariaRepository.getSorteoDiariaById(id), userRepository.getById(userId));
        apuestaRepository.delete(apuesta);
        return ResponseEntity.ok("Numero eliminado");
    }

    @PostMapping("/historial")
    @PreAuthorize("hasRole('USER')or hasRole('ASIS')")
    public List<HistorialUserResponse> getUserHitorial(@Valid @RequestBody ObjectNode json) {
        User user = getUserFromJsonNode(json);
        List<HistorialUserResponse> responses = new ArrayList<>();
        List<Sorteo> sorteos = sorteoRepository
                .findTop60ByEstadoOrderByIdDesc(estadoRepository.getEstadoByEstado(EstadoName.BLOQUEADA));

        double[] total = {0.0};
        double[] comisiones = {0.0};
        double[] premio = {0.0};

        sorteos.forEach(sorteo -> {
            if (user instanceof Asistente) {
                SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(sorteo.getId());
                if (sorteoDiaria != null) {
                    Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
                    apuestas.forEach(apuesta -> {
                        total[0] += apuesta.getCantidad();
                    });
                } else {
                    List<HistoricoApuestas> apuestasList = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, user);
                    apuestasList.forEach(historicoApuestas -> {
                        total[0] += historicoApuestas.getCantidad();
                    });
                }
            } else if (user instanceof Jugador) {
                Resultado resultado = resultadoRepository.getBySorteoAndUser(sorteo, user);
                total[0] += resultado.getCantApuesta();
                comisiones[0] += resultado.getComision();
                premio[0] += resultado.getPremio();
            }
            HistorialUserResponse userResponse = new HistorialUserResponse();
            userResponse.setId(sorteo.getId());
            userResponse.setTitle(Util.formatTimestamp2String(sorteo.getSorteoTime()));
            userResponse.setNumero(numeroGanadorRepository.getBySorteo(sorteo).getNumeroGanador());
            userResponse.setTotal(total[0]);
            userResponse.setComision(comisiones[0]);
            userResponse.setPremio(premio[0]);
            userResponse.setBalance(premio[0] + comisiones[0] - total[0]);
            responses.add(userResponse);
        });
        return responses;
    }

    @PostMapping("/historial/apuesta/{id}")
    @PreAuthorize("hasRole('USER')or hasRole('ASIS')")
    public List<PairNV> getUserHitorialByApuesta(@PathVariable Long id,
                                                 @Valid @RequestBody ObjectNode json) {
        User user = getUserFromJsonNode(json);
        List<PairNV> responses = new ArrayList<>();
        Sorteo sorteo = sorteoRepository.getSorteoById(id);
        double[] apuestas = new double[100];

        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Jugador jugador = null;
        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        double costoMilDiaria = 1 ;
        double costoMilChica = 1 ;
        double costoPedazoChica = 1 ;
        if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
            costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 ;
        }else{
            costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1 ;
            costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1 ;
        }
        double finalCostoMilDiaria = costoMilDiaria;
        double finalCostoMilChica = costoMilChica;
        double finalCostoPedazoChica = costoPedazoChica;
        if (sorteoDiaria != null) {
            Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
            apuestaList.forEach(apuesta -> {
                apuestas[apuesta.getNumero()] += apuesta.getCantidad() * finalCostoMilDiaria * finalCostoMilChica * finalCostoPedazoChica ;
            });

            if (user instanceof Jugador) {
                List<Asistente> asistentes = asistenteRepository.findAllByJugadorAndUserState((Jugador) user, UserState.ACTIVE);
                asistentes.forEach(asistente -> {
                    Set<Apuesta> apuestaList1 = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
                    apuestaList1.forEach(apuesta -> {
                        apuestas[apuesta.getNumero()] += apuesta.getCantidad() * finalCostoMilDiaria * finalCostoMilChica * finalCostoPedazoChica;
                    });
                });
            }
        } else {
            List<HistoricoApuestas> historicoApuestas = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, user);
            historicoApuestas.forEach(historico -> {
                apuestas[historico.getNumero()] += historico.getCantidad();
            });
            if (user instanceof Jugador) {
                List<Asistente> asistentes = asistenteRepository.findAllByJugadorAndUserState((Jugador) user, UserState.ACTIVE);
                asistentes.forEach(asistente -> {
                    List<HistoricoApuestas> apuestaList1 = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, asistente);
                    apuestaList1.forEach(apuesta -> {
                        apuestas[apuesta.getNumero()] += apuesta.getCantidad();
                    });
                });
            }
        }

        for (int i = 0; i < 100; i++) {
            if (apuestas[i] != 0) {
                responses.add(new PairNV(i, apuestas[i]));
            }
        }

        return responses;
    }

    @PostMapping("/historial/apuesta/{id}/detalles")
    @PreAuthorize("hasRole('USER') or hasRole('ASIS')")
    public List<ApuestaActivaDetallesResponse> getDetallesApuestaHistorial(@PathVariable Long id,
                                                                           @Valid @RequestBody ObjectNode json) {
        List<ApuestaActivaDetallesResponse> apuestasDetails = new ArrayList<>();
        User user = getUserFromJsonNode(json);
        Sorteo sorteo = sorteoRepository.getSorteoById(id);
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoDiariaById(id);
        Jugador jugador = null;
        if(user instanceof Jugador){
            jugador = (Jugador) user;
        }else{
            jugador = ((Asistente) user).getJugador();
        }
        double costoMilDiaria = 1 ;
        double costoMilChica = 1 ;
        double costoPedazoChica = 1 ;
        if(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA)){
            costoMilDiaria = jugador.getCostoMil() != 0 ? jugador.getCostoMil() : 1 ;
        }else{
            costoMilChica = jugador.getCostoChicaMiles() != 0 ? jugador.getCostoChicaMiles() : 1 ;
            costoPedazoChica = jugador.getCostoChicaPedazos() != 0 ? jugador.getCostoChicaPedazos() : 1 ;
        }
        double finalCostoMilDiaria = costoMilDiaria;
        double finalCostoMilChica = costoMilChica;
        double finalCostoPedazoChica = costoPedazoChica;
        if (sorteoDiaria != null) {
            Set<Apuesta> apuestas = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, user);
            List<PairNV> pairNVList = new ArrayList<>();
            double total = 0;
            for (Apuesta apuesta : apuestas) {
                total += apuesta.getCantidad()* finalCostoMilDiaria * finalCostoMilChica * finalCostoPedazoChica;
                pairNVList.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()* finalCostoMilDiaria * finalCostoMilChica * finalCostoPedazoChica));
            }
            ApuestaActivaDetallesResponse detallesResponse = new ApuestaActivaDetallesResponse();
            detallesResponse.setApuestas(pairNVList);
            detallesResponse.setTotal(total);
            detallesResponse.setTitle("Apuestas mias - " + getUsernameStringFromObjectNode(json));
            apuestasDetails.add(detallesResponse);
            List<Asistente> asistentes = asistenteRepository.findAllByJugadorAndUserState(jugador, UserState.ACTIVE);
            asistentes.forEach(asistente -> {
                Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
                if (apuestaList.size() > 0) {
                    ApuestaActivaDetallesResponse detallesResponse1 = new ApuestaActivaDetallesResponse();
                    List<PairNV> pairNVList1 = new ArrayList<>();
                    double total1 = 0;
                    for (Apuesta apuesta : apuestaList) {
                        total1 += apuesta.getCantidad() * finalCostoMilDiaria * finalCostoMilChica * finalCostoPedazoChica;
                        pairNVList1.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad() * finalCostoMilDiaria * finalCostoMilChica * finalCostoPedazoChica));
                    }
                    detallesResponse1.setApuestas(pairNVList1);
                    detallesResponse1.setTotal(total1);
                    detallesResponse1.setTitle("Apuestas de " + asistente.getUsername());
                    apuestasDetails.add(detallesResponse1);
                }
            });
        } else {
            List<HistoricoApuestas> apuestas = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, user);
            List<PairNV> pairNVList = new ArrayList<>();
            double total = 0;
            for (HistoricoApuestas apuesta : apuestas) {
                total += apuesta.getCantidad();
                pairNVList.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
            }
            ApuestaActivaDetallesResponse detallesResponse = new ApuestaActivaDetallesResponse();
            detallesResponse.setApuestas(pairNVList);
            detallesResponse.setTotal(total);
            detallesResponse.setTitle("Apuestas mias - " + getUsernameStringFromObjectNode(json));
            apuestasDetails.add(detallesResponse);
            List<Asistente> asistentes = asistenteRepository.findAllByJugadorAndUserState((Jugador) user, UserState.ACTIVE);
            asistentes.forEach(asistente -> {
                List<HistoricoApuestas> apuestaList = historicoApuestaRepository.findAllBySorteoAndUser(sorteo, asistente);
                if (apuestaList.size() > 0) {
                    ApuestaActivaDetallesResponse detallesResponse1 = new ApuestaActivaDetallesResponse();
                    List<PairNV> pairNVList1 = new ArrayList<>();
                    double total1 = 0;
                    for (HistoricoApuestas apuesta : apuestaList) {
                        total1 += apuesta.getCantidad();
                        pairNVList1.add(new PairNV(apuesta.getNumero(), apuesta.getCantidad()));
                    }
                    detallesResponse1.setApuestas(pairNVList1);
                    detallesResponse1.setTotal(total1);
                    detallesResponse1.setTitle("Apuestas de " + asistente.getUsername());
                    apuestasDetails.add(detallesResponse1);
                }
            });
        }

        return apuestasDetails;
    }


    @PostMapping("/balance")
    @PreAuthorize("hasRole('USER')")
    public ObjectNode getJugadorBalance(@Valid @RequestBody ObjectNode json) {
    	User user = getUserFromJsonNode(json);
    	Jugador jugador = Util.getJugadorFromUser(user);
    	
    	ObjectMapper mapper = new ObjectMapper();
    	ObjectNode node = mapper.createObjectNode();
    	node.put("balance", jugador.getBalance());
    	node.put("currency", jugador.getMoneda().getMonedaName().compareTo(MonedaName.LEMPIRA)==0?"L":"$");
        return node;
    }
    
    @GetMapping("/balance")
    @PreAuthorize("hasRole('USER')")
    public CurrentJugadorBalance getJugadorBalance() {
    	User user = userService.getLoggedInUser();
    	Jugador jugador = Util.getJugadorFromUser(user);
    	CurrentJugadorBalance result = new CurrentJugadorBalance(jugador.getMoneda().getMonedaName().toString(), jugador.getBalance());
        
    	return result;
    }

    private void updateDataApuestaAndRestriccion(Restriccion restriccion, int diff, Apuesta apuesta,
                                                 double current, double comision, double costoMil, double costoPedazo) {

        if (current * costoMil * costoPedazo <= diff) {
            apuesta.setCantidad(current);
            apuesta.setComision(current * costoPedazo * comision / 100);
            restriccion.setPuntosCurrent((int) current);
        } else {
            double restrccionValue = (double) restriccion.getPuntos();
            apuesta.setCantidad(restrccionValue);
            apuesta.setComision(restrccionValue * costoPedazo *comision / 100);
            restriccion.setPuntosCurrent(restriccion.getPuntos());
        }
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

    private void generateTopes(List<NumeroPlayerEntryResponse> topes, List<Restriccion> restriciones) {
        for (int i = 0; i < 100; i++) {
            boolean flag = false;
            for (Restriccion restriccion : restriciones) {
                if (restriccion.getNumero() == i) {
                    if (i < 10) {
                        topes.add(new NumeroPlayerEntryResponse("0" + i,
                                restriccion.getPuntos(), restriccion.getPuntosCurrent()));
                    } else {
                        topes.add(new NumeroPlayerEntryResponse(String.valueOf(i),
                                restriccion.getPuntos(), restriccion.getPuntosCurrent()));
                    }
                    flag = true;
                }
            }
            if (!flag && i < 10) {
                topes.add(new NumeroPlayerEntryResponse("0" + i, 0, 0));
            } else if (!flag) {
                topes.add(new NumeroPlayerEntryResponse(String.valueOf(i), 0, 0));
            }
        }
    }


    static ApuestaActivaResponse getApuestaActivaResponsePlayer(@RequestBody @Valid ObjectNode json,
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
            List<Asistente> asistentes = asistenteRepository.findAllByJugadorAndUserState(jugador, UserState.ACTIVE);
            asistentes.forEach(asistente -> {
                Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiariaAndUser(sorteoDiaria, asistente);
                if (!apuestaList.isEmpty()) {
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
        apuestaActivaResponse.setDay(Util.getDayFromTimestamp(sorteoDiaria.getSorteoTime()));
        apuestaActivaResponse.setHour(Util.getHourFromTimestamp(sorteoDiaria.getSorteoTime()));
        apuestaActivaResponse.setTotal(total[0]);
        apuestaActivaResponse.setComision(comision);
        apuestaActivaResponse.setRiesgo(total[0] - comision);
        apuestaActivaResponse.setType(sorteoDiaria.getSorteo().getSorteoType().getSorteoTypeName().toString());
        return apuestaActivaResponse;
    }
    
}
