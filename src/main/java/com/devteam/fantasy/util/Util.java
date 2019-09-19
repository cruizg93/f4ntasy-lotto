package com.devteam.fantasy.util;

import com.devteam.fantasy.model.Apuesta;
import com.devteam.fantasy.model.Asistente;
import com.devteam.fantasy.model.HistoricoApuestas;
import com.devteam.fantasy.model.Jugador;
import com.devteam.fantasy.model.Sorteo;
import com.devteam.fantasy.model.SorteoDiaria;
import com.devteam.fantasy.model.SorteoType;
import com.devteam.fantasy.model.Status;
import com.devteam.fantasy.model.User;
import com.devteam.fantasy.repository.ApuestaRepository;
import com.devteam.fantasy.repository.EstadoRepository;
import com.devteam.fantasy.repository.HistoricoApuestaRepository;
import com.devteam.fantasy.repository.JugadorRepository;
import com.devteam.fantasy.repository.SorteoDiariaRepository;
import com.devteam.fantasy.repository.SorteoRepository;
import com.devteam.fantasy.repository.SorteoTypeRepository;
import com.devteam.fantasy.repository.StatusRepository;
import com.devteam.fantasy.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static java.time.temporal.TemporalAdjusters.nextOrSame;
import static java.time.temporal.TemporalAdjusters.previousOrSame;

import java.math.BigDecimal;

public class Util {

    private static TimeDate time = TimeDate.getInstance();
    private static LocalDate ld = LocalDate.now(time.getZ());
    private static String[] months = {"Enero", "Febrero", "Marzo", "Abril",
            "Mayo", "Junio", "Julio",
            "Agosto", "Septiembre", "Octubre",
            "Noviembre", "Diciembre"};
    private static String[] monthsAbb = {"Ene", "Feb", "Mar", "Abr",
            "May", "Jun", "Jul",
            "Ago", "Sep", "Oct",
            "Nov", "Dic"};
    private static String[] weekNamesAbb = {"Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom"};

    private static String[] weekNames = {"Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sábado", "Domingo"};


    private static String[] times = {"11 am", "3 pm", "9 pm", "Chica"};

    public static LocalDate getTodayTime() {
        return LocalDate.now(time.getZ());
    }

    public static void updateJugadoBalance(JugadorRepository jugadorRepository) {
        List<Jugador> jugadors = jugadorRepository.findAll();
        jugadors.forEach(jugador -> {
            jugador.setBalance(0);
            jugadorRepository.save(jugador);
        });
    }

    public static void updateSorteoStatus(SorteoRepository sorteoRepository,
                                          StatusRepository statusRepository,
                                          SorteoDiariaRepository sorteoDiariaRepository
    ) {
        Status current = statusRepository.getByStatus(StatusName.CURRENT);
        Status last = statusRepository.getByStatus(StatusName.LAST);
        Status other = statusRepository.getByStatus(StatusName.OTHER);
        List<Sorteo> sorteos = sorteoRepository.findAllByStatus(last);
        sorteos.forEach(sorteo -> {
            sorteo.setStatus(other);
            sorteoRepository.save(sorteo);
        });
        sorteos = sorteoRepository.findAllByStatus(current);
        sorteos.forEach(sorteo -> {
            if (!sorteoDiariaRepository.existsSorteoDiariaBySorteo(sorteo)) {
                sorteo.setStatus(last);
                sorteoRepository.save(sorteo);
            }
        });
    }

    public static void insertSorteo(EstadoRepository estadoRepository,
                                    SorteoRepository sorteoRepository,
                                    SorteoDiariaRepository sorteoDiariaRepository,
                                    SorteoTypeRepository sorteoTypeRepository,
                                    SorteoTypeName sorteoTypeName,
                                    int hour) {
        Sorteo sorteo = new Sorteo();
        sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.ABIERTA));

        Timestamp timestamp;
        if (sorteoTypeName.equals(SorteoTypeName.CHICA)){
//            Timestamp timestamp=Timestamp.va;
            LocalDate localDate=LocalDate.now().with( next( SUNDAY ) );
            timestamp = Timestamp.valueOf(LocalDateTime.of(localDate, LocalTime.NOON));
        }else{
            timestamp = new Timestamp(
                    ZonedDateTime.of(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), hour, 0, 0, 0,
                            ZoneId.of("America/Tegucigalpa")
                    ).toInstant().toEpochMilli()
            );
        }
        sorteo.setSorteoTime(timestamp);
        sorteo.setSorteoType(sorteoTypeRepository.getBySorteoTypeName(sorteoTypeName));
        sorteoRepository.save(sorteo);
        SorteoDiaria sorteoDiaria1 = new SorteoDiaria();
        sorteoDiaria1.setId(sorteo.getId());
        sorteoDiaria1.setSorteo(sorteo);
        sorteoDiaria1.setSorteoTime(timestamp);
        sorteoDiariaRepository.save(sorteoDiaria1);

    }


    public static void newSorteo(EstadoRepository estadoRepository,
            SorteoRepository sorteoRepository,
            SorteoDiariaRepository sorteoDiariaRepository,
            SorteoTypeRepository sorteoTypeRepository,
            SorteoTypeName sorteoTypeName,
            Timestamp horaSorteoCerrado) {
        Sorteo sorteo = new Sorteo();
        sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.ABIERTA));

        Timestamp timestamp;
        if (sorteoTypeName.equals(SorteoTypeName.CHICA)){
            //            Timestamp timestamp=Timestamp.va;
        	LocalDateTime horaSorteoNuevo = horaSorteoCerrado.toLocalDateTime().plusDays(7);
            timestamp = Timestamp.valueOf(horaSorteoNuevo);
        }else{
        	LocalDateTime horaSorteoNuevo = horaSorteoCerrado.toLocalDateTime().plusDays(1);
        	timestamp = Timestamp.valueOf(horaSorteoNuevo);
        }
        sorteo.setSorteoTime(timestamp);
        sorteo.setSorteoType(sorteoTypeRepository.getBySorteoTypeName(sorteoTypeName));
        sorteoRepository.save(sorteo);
        SorteoDiaria sorteoDiaria1 = new SorteoDiaria();
        sorteoDiaria1.setId(sorteo.getId());
        sorteoDiaria1.setSorteo(sorteo);
        sorteoDiaria1.setSorteoTime(timestamp);
        sorteoDiariaRepository.save(sorteoDiaria1);

    }
    
    private static void newSorteo(EstadoRepository estadoRepository, SorteoRepository sorteoRepository,
			SorteoDiariaRepository sorteoDiariaRepository, SorteoTypeRepository sorteoTypeRepository,
			SorteoTypeName sorteoTypeName, int hour) {

    	Timestamp horaSorteoNuevo= new Timestamp(
                ZonedDateTime.of(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), hour, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        );
    	
    	newSorteo(estadoRepository,
                sorteoRepository,
                sorteoDiariaRepository,
                sorteoTypeRepository,
                sorteoTypeName,
                horaSorteoNuevo);
	}

    public static void deleteSorteo(EstadoRepository estadoRepository,
                                    SorteoRepository sorteoRepository,
                                    SorteoDiariaRepository sorteoDiariaRepository,
                                    int hour) {
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoActivoBySorteoTime(new Timestamp(
                ZonedDateTime.of(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), hour, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));
        if (sorteoDiaria != null) {
            Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
            sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
            sorteoRepository.save(sorteo);
            sorteoDiariaRepository.delete(sorteoDiaria);
        }
    }

    public static void updateSorteo(EstadoRepository estadoRepository,
                                    SorteoRepository sorteoRepository,
                                    SorteoDiariaRepository sorteoDiariaRepository,
                                    int hour) {
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoActivoBySorteoTime(new Timestamp(
                ZonedDateTime.of(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), hour, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));
        if (sorteoDiaria != null) {
            Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
            sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
            sorteoRepository.save(sorteo);
        }
    }

    public static void updateChicaSorteo(EstadoRepository estadoRepository,
                                         SorteoRepository sorteoRepository,
                                         SorteoTypeRepository sorteoTypeRepository
    ) {

        Sorteo sorteo = sorteoRepository.getSorteoBySorteoTypeEquals(sorteoTypeRepository
                .getBySorteoTypeName(SorteoTypeName.CHICA));
        if (sorteo != null) {
            sorteo.setEstado(estadoRepository.getEstadoByEstado(EstadoName.CERRADA));
            sorteoRepository.save(sorteo);
        }
    }

    public static void deleteAllDiariaSorteo(SorteoDiariaRepository sorteoDiariaRepository) {
        deleteSpecificDiariaSorteo(sorteoDiariaRepository, 11);
        deleteSpecificDiariaSorteo(sorteoDiariaRepository, 15);
        deleteSpecificDiariaSorteo(sorteoDiariaRepository, 21);
    }

    public static User getUserFromJsonNode(UserRepository userRepository, ObjectNode json) {
        ObjectMapper mapper = new ObjectMapper();
        String username = mapper.convertValue(json.get("username"), String.class);
        return userRepository.getByUsername(username);
    }

    private static void deleteSpecificDiariaSorteo(
            SorteoDiariaRepository sorteoDiariaRepository,
            int hour) {
        SorteoDiaria sorteoDiaria = sorteoDiariaRepository.getSorteoActivoBySorteoTime(new Timestamp(
                ZonedDateTime.of(ld.getYear(), ld.getMonthValue(), ld.getDayOfMonth(), hour, 0, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));
        if (sorteoDiaria != null) {
            sorteoDiariaRepository.delete(sorteoDiaria);
        }
    }
    
    private static void deleteSpecificDiariaSorteo(SorteoDiariaRepository sorteoDiariaRepository, SorteoDiaria sorteoDiaria) {
        if (sorteoDiaria != null) {
            sorteoDiariaRepository.delete(sorteoDiaria);
        }
    }
    
    private static void deleteChicaSorteo(SorteoDiariaRepository sorteoDiariaRepository) {
        Iterable<SorteoDiaria> sorteoDiarias = sorteoDiariaRepository.findAll();

        List<SorteoDiaria> listsSorteoDiaria = new ArrayList<>();
        sorteoDiarias.iterator().forEachRemaining(listsSorteoDiaria::add);

        Optional<SorteoDiaria> sorteoDiaria = listsSorteoDiaria.stream()
                .filter(sorteo -> sorteo.getSorteo().getSorteoType().getSorteoTypeName().equals(SorteoTypeName.CHICA))
                .findFirst();
        sorteoDiaria.ifPresent(sorteoDiariaRepository::delete);
    }



    public static void deleteAndCreateSorteoDiaria(
            EstadoRepository estadoRepository,
            SorteoRepository sorteoRepository,
            SorteoTypeRepository sorteoTypeRepository,
            SorteoTypeName sorteoTypeName,
            SorteoDiariaRepository sorteoDiariaRepository,
            SorteoDiaria sorteoDiaria){
        String time=sorteoDiaria.getSorteoTime().toString();
        if(time.contains("11:00")){
//            deleteSpecificDiariaSorteo(sorteoDiariaRepository, 11);
            deleteSpecificDiariaSorteo(sorteoDiariaRepository,sorteoDiaria);
            newSorteo(estadoRepository,sorteoRepository,sorteoDiariaRepository,sorteoTypeRepository,sorteoTypeName, sorteoDiaria.getSorteoTime());
        }else if(time.contains("15:00")){
//            deleteSpecificDiariaSorteo(sorteoDiariaRepository, 15);
        	deleteSpecificDiariaSorteo(sorteoDiariaRepository,sorteoDiaria);
            newSorteo(estadoRepository,sorteoRepository,sorteoDiariaRepository,sorteoTypeRepository,SorteoTypeName.DIARIA, sorteoDiaria.getSorteoTime());
        }else if(time.contains("21:00")){
//            deleteSpecificDiariaSorteo(sorteoDiariaRepository, 21);
        	deleteSpecificDiariaSorteo(sorteoDiariaRepository,sorteoDiaria);
            newSorteo(estadoRepository,sorteoRepository,sorteoDiariaRepository,sorteoTypeRepository,SorteoTypeName.DIARIA, sorteoDiaria.getSorteoTime());
        }else {
            deleteChicaSorteo(sorteoDiariaRepository);
            newSorteo(estadoRepository,sorteoRepository,sorteoDiariaRepository,sorteoTypeRepository,SorteoTypeName.CHICA, sorteoDiaria.getSorteoTime());
        }
    }

	public static void deleteSorteosDia(SorteoRepository sorteoRepository,
                                        SorteoDiariaRepository sorteoDiariaRepository,
                                        ApuestaRepository apuestaRepository,
                                        HistoricoApuestaRepository historicoApuestaRepository
    ) {
        Iterable<SorteoDiaria> sorteoDiariaList = sorteoDiariaRepository.findAll();
        sorteoDiariaList.forEach(sorteoDiaria -> {
            Sorteo sorteo = sorteoRepository.getSorteoById(sorteoDiaria.getId());
            if (sorteo.getEstado().getEstado().equals(EstadoName.BLOQUEADA)) {
                Set<Apuesta> apuestaList = apuestaRepository.findAllBySorteoDiaria(sorteoDiaria);
                apuestaList.forEach(apuesta -> {
                    HistoricoApuestas historicoApuestas = new HistoricoApuestas();
                    historicoApuestas.setCantidad(apuesta.getCantidad());
                    historicoApuestas.setUser(apuesta.getUser());
                    historicoApuestas.setSorteo(sorteo);
                    historicoApuestas.setNumero(apuesta.getNumero());
                    historicoApuestas.setComision(apuesta.getComision());
                    historicoApuestas.setCambio(apuesta.getCambio());
                    historicoApuestaRepository.save(historicoApuestas);
                    apuestaRepository.delete(apuesta);
                });
                sorteoDiariaRepository.delete(sorteoDiaria);
            }

        });


    }

    public static Timestamp getTodayTimeStamp() {
        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();
        int day = localDate.getDayOfMonth();

        return (new Timestamp(
                ZonedDateTime.of(year, month, day, 0, 1, 0, 0,
                        ZoneId.of("America/Tegucigalpa")
                ).toInstant().toEpochMilli()
        ));
    }

    public static String formatLocalDatetoString(LocalDate localDate, int pos) {
        return String.format("%s %d, %d - %s", months[localDate.getMonthValue() - 1],
                localDate.getDayOfMonth(), localDate.getYear(), times[pos]);
    }

    public static String formatTimestamp2String(Timestamp timestamp) {
        Integer time = Integer.valueOf(timestamp.toString().substring(11, 13));
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        String timeValue = "";
        switch (time) {
            case 15:
                timeValue = " - 3 pm";
                break;
            case 21:
                timeValue = "- 9 pm";
                break;
            case 11:
                timeValue = "- 11 am";
                break;
            default:
                break;
        }
        return String.format("%s - %s %d, %d %s", weekNames[localDate.getDayOfWeek().getValue() - 1],
                months[localDate.getMonthValue() - 1],
                localDate.getDayOfMonth(), localDate.getYear(), timeValue);
    }

    public static String formatLocalDate2StringShort(Timestamp timestamp) {
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        return String.format("%s %d", months[localDate.getMonthValue() - 1],
                localDate.getDayOfMonth());
    }

    public static String formatStringShortData2StringShort(String dateShort) {
        String[] parse = dateShort.split("-");
        return String.format("%s %d", months[Integer.valueOf(parse[1]) - 1],
                Integer.valueOf(parse[2]));
    }

    public static boolean compareToTimestamp(Timestamp timestamp, Timestamp timestamp1) {
        return timestamp.toString().substring(0, 10).equals(timestamp1.toString().substring(0, 10));
    }

    public static boolean compareStringToTimestamp(String date, Timestamp timestamp1) {
        return date.equals(timestamp1.toString().substring(0, 10));
    }

    public static String shortTimestamp(Timestamp timestamp) {
        return timestamp.toString().substring(0, 10);
    }

    public static String getLastWeekMondayAndSunday() {
        String monday = formatStringShortData2StringShort(LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY).toString());
        String sunday = formatStringShortData2StringShort(LocalDate.now().minusWeeks(1).with(DayOfWeek.SUNDAY).toString());
        return String.format("%s - %s", monday, sunday);
    }

    public static String getMondayAndSundayDay() {
        LocalDate today = LocalDate.now();

        LocalDate monday = today.with(previousOrSame(MONDAY));
        LocalDate sunday = today.with(nextOrSame(SUNDAY));

        return String.format("%s %d - %s %d", months[monday.getMonthValue() - 1],
                monday.getDayOfMonth(), months[sunday.getMonthValue() - 1],
                sunday.getDayOfMonth());
    }

    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() != 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }

    public static String formatDate2StringDate() {
        LocalDate localDate = getTodayTime();
        return String.format("%s, %d %s", weekNamesAbb[localDate.getDayOfWeek().getValue() - 1],
                localDate.getDayOfMonth(), monthsAbb[localDate.getMonthValue() - 1]);
    }

    public static String formatDate2StringTime() {
        LocalTime localTime = LocalTime.now();
    	DateTimeFormatter dtf = new DateTimeFormatterBuilder().appendPattern("hh : mm a").toFormatter();
        return dtf.format(localTime);
    }

    public static String formatTimestamp2StringApuestas(String sorteoType, Timestamp timestamp) {
        Integer time = Integer.valueOf(timestamp.toString().substring(11, 13));
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        String timeValue = "Chica";
        switch (time) {
            case 15:
                timeValue = "3 pm";
                break;
            case 21:
                timeValue = "9 pm";
                break;
            case 11:
                timeValue = "11 am";
                break;
            default:
                break;
        }
        if (timeValue.equals("Chica")) {
            return String.format("%s: %s - %s %d, %d", sorteoType,
                    weekNamesAbb[localDate.getDayOfWeek().getValue() - 1],
                    monthsAbb[localDate.getMonthValue() - 1],
                    localDate.getDayOfMonth(), localDate.getYear());
        }
        return String.format("%s - %s %s - %s %d, %d", sorteoType, timeValue,
                weekNamesAbb[localDate.getDayOfWeek().getValue() - 1],
                monthsAbb[localDate.getMonthValue() - 1],
                localDate.getDayOfMonth(), localDate.getYear());

    }

    public static String formatTimestamp2StringShortAbb(Timestamp timestamp) {
        Integer time = Integer.valueOf(timestamp.toString().substring(11, 13));
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        String timeValue = "Chica";
        switch (time) {
            case 15:
                timeValue = "3 pm";
                break;
            case 21:
                timeValue = "9 pm";
                break;
            case 11:
                timeValue = "11 am";
                break;
            default:
                break;
        }
        if (timeValue.equals("Chica")) {
            return String.format("%s , %d %s",
                    weekNamesAbb[localDate.getDayOfWeek().getValue() - 1],
                    localDate.getDayOfMonth(),
                    monthsAbb[localDate.getMonthValue() - 1]);
        }
        return String.format("%s , %d %s - %s",
                weekNamesAbb[localDate.getDayOfWeek().getValue() - 1],
                localDate.getDayOfMonth(),
                monthsAbb[localDate.getMonthValue() - 1],
                timeValue);
    }
    
    /**
     * @Author Cristian Ruiz
     * @Date 28/08/2019
     * @param timestamp
     * @return hour with am or pm
     */
    public static String getHourFromTimestamp(Timestamp timestamp) {
        Integer time = Integer.valueOf(timestamp.toString().substring(11, 13));
        String timeValue = "";
        switch (time) {
            case 15:
                timeValue = "3 pm";
                break;
            case 21:
                timeValue = "9 pm";
                break;
            case 11:
                timeValue = "11 am";
                break;
            default:
                break;
        }
        return timeValue;
    }
    
    /**
     * @Author Cristian Ruiz
     * @Date 28/08/2019
     * @param timestamp
     * @return day from timestamp
     */
    public static String getDayFromTimestamp(Timestamp timestamp) {
        LocalDate localDate = timestamp.toLocalDateTime().toLocalDate();
        return String.format("%s, %s %s", weekNamesAbb[localDate.getDayOfWeek().getValue() - 1],
        		localDate.getDayOfMonth(),
        		monthsAbb[localDate.getMonthValue() - 1]
                );
    }
    
    public static Jugador getJugadorFromApuesta(Apuesta apuesta) {
    	return getJugadorFromUser(apuesta.getUser());
    }
    
    public static Jugador getJugadorFromUser(User user) {
    	if(user instanceof Jugador){
    		return (Jugador) user;
        }else if(user instanceof Asistente){
            return ((Asistente) user).getJugador();
        }
    	return null;
    }
    
    public static BigDecimal getApuestaCambio(String currency, Apuesta apuesta ) {
    	BigDecimal cambio= BigDecimal.ONE;

        Jugador jugador = Util.getJugadorFromApuesta(apuesta);
        if(currency.equalsIgnoreCase("lempira") && jugador.getMoneda().getMonedaName().equals(MonedaName.DOLAR)){
            cambio = new BigDecimal(apuesta.getCambio().getCambio());
        }else if(currency.equalsIgnoreCase("dolar") && jugador.getMoneda().getMonedaName().equals(MonedaName.LEMPIRA)){
            cambio = new BigDecimal(1/apuesta.getCambio().getCambio());
        }
        return cambio;
    }
    
    public static boolean isSorteoTypeDiaria(Sorteo sorteo) {
    	return sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.DIARIA);
    }
    
    public static boolean isSorteoTypeChica(Sorteo sorteo) {
    	return sorteo.getSorteoType().getSorteoTypeName().equals(SorteoTypeName.CHICA);
    }

	public static MonedaName getMonedaNameFromString(String monedaType) {
		return MonedaName.LEMPIRA.toString().equalsIgnoreCase(monedaType)?MonedaName.LEMPIRA:MonedaName.DOLAR;
	}
	
	public static BigDecimal getPremioMultiplier(Jugador jugador, SorteoTypeName sorteoType) {
		BigDecimal premio = BigDecimal.valueOf(jugador.getPremioDirecto());
		
		if(sorteoType.equals(SorteoTypeName.DIARIA)){
        	if(jugador.getTipoApostador().getApostadorName().equals(ApostadorName.MILES)){
            	premio = BigDecimal.valueOf(jugador.getPremioMil());
        	}
        }else if(jugador.getTipoChica().getChicaName().equals(ChicaName.MILES)){
        	premio = BigDecimal.valueOf(jugador.getPremioChicaMiles());
        }else if(jugador.getTipoChica().getChicaName().equals(ChicaName.PEDAZOS)){
        	premio = BigDecimal.valueOf(jugador.getPremioChicaPedazos());
        }
		
		return premio;
	}
}



















