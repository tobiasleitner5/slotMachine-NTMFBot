package testdatagenerator;

import at.jku.dke.slotmachine.nmf.service.dto.*;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.mifmif.common.regex.Generex;
import connectivity.JsonInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDataGenerator.class);
    private static final Random random = new Random();

    public static EnvelopeDTO generateEnvelopeDTO(){

        TestDataConfigDTO testDataConfigDTO = JsonInput.readConfig("config/config.json");
        List<TestDataConfigElement> testDataConfigElements = testDataConfigDTO.getTestDataConfigElements();
        TestDataConfigGlobal testDataConfigGlobal = testDataConfigDTO.getTestDataConfigGlobal();
        EnvelopeDTO envelopeDTO = new EnvelopeDTO();
        BodyDTO bodyDTO = new BodyDTO();
        FlightListByAerodromeReplyDTO flightListByAerodromeReplyDTO = new FlightListByAerodromeReplyDTO();
        DataDTO dataDTO = new DataDTO();
        List<FlightsDTO> flightsDTOList;

        //global settings
        String aerodomeOfRegulation = testDataConfigGlobal.getAerodomeOfRegulation();
        String mostPenalisingRegulationCauseReason = testDataConfigGlobal.getMostPenalisingRegulationCauseReason();
        String filledRegistrationMark = testDataConfigGlobal.getFilledRegistrationMark();
        int slotSwapCounterCurrent = testDataConfigGlobal.getSlotSwapCounterCurrent();
        int slotSwapCounterMax = testDataConfigGlobal.getSlotSwapCounterMax();
        String mostPenalisingRegulationLocationCategory = testDataConfigGlobal.getMostPenalisingRegulationLocationCategory();
        String status = testDataConfigGlobal.getStatus();
        boolean nonICAOAerodromeOfDestination = testDataConfigGlobal.getNonICAOAerodromeOfDestination();
        boolean nonICAOAerodromeOfDeparture = testDataConfigGlobal.getNonICAOAerodromeOfDeparture();
        boolean airfiled = testDataConfigGlobal.getAirfiled();
        String timeWindowString = testDataConfigGlobal.getTimeWindowString();

        //RequestReceptionTime
        LocalDateTimeToMinOrSecDTO localDateTimeToMinOrSecDTO = new LocalDateTimeToMinOrSecDTO();
        localDateTimeToMinOrSecDTO.setText(LocalDateTime.now());

        //requestID
        Generex requestId = new Generex("[A-Z][0-9][A-Z]_[A-Z]{3}:[0-9]{8}");
        String requestIdString = requestId.random();

        flightListByAerodromeReplyDTO.setRequestReceptionTime(localDateTimeToMinOrSecDTO);
        flightListByAerodromeReplyDTO.setRequestId(requestIdString);
        flightListByAerodromeReplyDTO.setSendTime(localDateTimeToMinOrSecDTO);
        flightListByAerodromeReplyDTO.setStatus(testDataConfigGlobal.getStatus());

        //dataDTO
        //

        Generex randomAerodrome = new Generex("[A-Z]{4}");
        String aerodromeOfDeparture = null;
        String aerodromeOfArrival = null;
        if(testDataConfigGlobal.getMostPenalisingRegulationLocationCategory().equals("DEPARTURE")){
            aerodromeOfDeparture = aerodomeOfRegulation;
            aerodromeOfArrival = randomAerodrome.random();
        } else if (testDataConfigGlobal.getMostPenalisingRegulationLocationCategory().equals("ARRIVAL")){
            aerodromeOfArrival = aerodomeOfRegulation;
            aerodromeOfDeparture = randomAerodrome.random();
        }
        else{
            LOGGER.error("Error with Location Category!");
        }

        //RandomDate
        String [] s = timeWindowString.split("->");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime start = LocalDateTime.parse(s[0], timeFormatter);
        LocalDateTime end = LocalDateTime.parse(s[1], timeFormatter);

        //generate list of times
        List<LocalDateTime> l = TestDataGeneratorTimes.generateTimeList(start, end);

        SlotSwapCounterDTO slotSwapCounterDTO = new SlotSwapCounterDTO();
        slotSwapCounterDTO.setCurrentCounter(Integer.toString(slotSwapCounterCurrent));
        slotSwapCounterDTO.setMaxLimit(Integer.toString(slotSwapCounterMax));

        int flightId = 1000;
        Generex aircraftId = new Generex("AIR[A-Z]{4}");
        Generex aircraftType = new Generex("A[0-9]{3}");
        List<FlightsDTO> flights = new ArrayList<>();
        for(TestDataConfigElement element : testDataConfigElements){
            for(int i = 0; i < element.getNumberOfFlights(); i++) {
                FlightsDTO flightsDTO = new FlightsDTO();
                FlightDTO flightDTO = new FlightDTO();
                FlightIdDTO flightIdDTO = new FlightIdDTO();
                KeysDTO keysDTO = new KeysDTO();
                LocalDateTime calculatedTakeoffTime = TestDataGeneratorTimes.pickRandomDate(l);
                LocalDateTimeToMinOrSecDTO calculatedTakeoffTimeNew = new LocalDateTimeToMinOrSecDTO();
                calculatedTakeoffTimeNew.setText(calculatedTakeoffTime);
                LocalDateTimeToMinOrSecDTO arrivalTime = new LocalDateTimeToMinOrSecDTO();
                arrivalTime.setText(calculatedTakeoffTime.plus(random.nextInt(300), ChronoUnit.MINUTES));

                //keys
                keysDTO.setAircraftId(aircraftId.random());
                keysDTO.setAerodromeOfDeparture(aerodromeOfDeparture);
                keysDTO.setAerodromeOfDestination(aerodromeOfArrival);
                keysDTO.setNonICAOAerodromeOfDeparture(nonICAOAerodromeOfDeparture);
                keysDTO.setNonICAOAerodromeOfDestination(nonICAOAerodromeOfDestination);
                keysDTO.setAirFiled(airfiled);
                LocalDateTimeToMinOrSecDTO estimatedOffBlockTime = new LocalDateTimeToMinOrSecDTO();
                estimatedOffBlockTime.setText(calculatedTakeoffTime.plus(20, ChronoUnit.MINUTES));
                keysDTO.setEstimatedOffBlockTime(estimatedOffBlockTime);

                //flightIdDTO
                flightIdDTO.setFlightId(Integer.toString(flightId));
                flightIdDTO.setKeys(keysDTO);

                //mostpenalisingRegulationCause
                MostPenalisingRegulationCauseDTO mostPenalisingRegulationCauseDTO = new MostPenalisingRegulationCauseDTO();
                mostPenalisingRegulationCauseDTO.setReason(mostPenalisingRegulationCauseReason);
                mostPenalisingRegulationCauseDTO.setLocationCategory(mostPenalisingRegulationLocationCategory);

                //flightDTO
                flightDTO.setFlightId(flightIdDTO);
                flightDTO.setAircraftType(aircraftType.random());
                flightDTO.setEstimatedTakeOffTime(calculatedTakeoffTimeNew);
                flightDTO.setCalculatedTakeOffTime(calculatedTakeoffTimeNew);
                flightDTO.setEstimatedTimeOfArrival(arrivalTime);
                flightDTO.setAircraftOperator(element.getAirline());
                flightDTO.setOperatingAircraftOperator(element.getAirline());
                flightDTO.setMostPenalisingRegulation("REGULATION1");
                flightDTO.setMostPenalisingRegulationCause(mostPenalisingRegulationCauseDTO);
                flightDTO.setFiledRegistrationMark(filledRegistrationMark);
                flightDTO.setSlotSwapCounter(slotSwapCounterDTO);

                //flights
                flightsDTO.setFlight(flightDTO);
                flights.add(flightsDTO);
            }
        }
        dataDTO.setFlights(flights);
        EffectiveTrafficWindowDTO effectiveTrafficWindowDTO = new EffectiveTrafficWindowDTO();
        effectiveTrafficWindowDTO.setWef(start.toString());
        effectiveTrafficWindowDTO.setUnt(end.toString());
        dataDTO.setEffectiveTrafficWindow(effectiveTrafficWindowDTO);

        flightListByAerodromeReplyDTO.setData(dataDTO);
        bodyDTO.setFlightListByAerodromeReply(flightListByAerodromeReplyDTO);
        envelopeDTO.setBody(bodyDTO);

        try {
            toxml(envelopeDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(envelopeDTO);

        return envelopeDTO;
    }

    public static void toxml(EnvelopeDTO input) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.findAndRegisterModules();
        xmlMapper.writeValue(new File("simple_bean.xml"), input);
        File file = new File("simple_bean.xml");
    }

    /*private static void checkInput(int numberOfFlights, int numberOfSlots, String timeWindowString) throws IOException {
        if((numberOfFlights >= 10) && (numberOfSlots >= 10) && numberOfSlots >= numberOfFlights && timeWindowString.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}->[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}")){
            LOGGER.info("Input is OK.");
        }
        else{
            throw new IOException("Input is not valid.");
        }
    }*/
}