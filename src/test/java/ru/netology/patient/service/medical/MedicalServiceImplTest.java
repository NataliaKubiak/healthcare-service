package ru.netology.patient.service.medical;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import ru.netology.patient.entity.BloodPressure;
import ru.netology.patient.entity.HealthInfo;
import ru.netology.patient.entity.PatientInfo;
import ru.netology.patient.repository.PatientInfoFileRepository;
import ru.netology.patient.repository.PatientInfoRepository;
import ru.netology.patient.service.alert.SendAlertService;
import ru.netology.patient.service.alert.SendAlertServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicalServiceImplTest {

    private PatientInfoRepository patientInfoRepository;
    private SendAlertService alertService;
    private MedicalServiceImpl medicalService;

    private final String id = "123";
    private final String expectedMsg = String.format("Warning, patient with id: %s, need help", id);

    @BeforeEach
    public void setUp() {
        patientInfoRepository = Mockito.mock(PatientInfoFileRepository.class);
        alertService = Mockito.mock(SendAlertServiceImpl.class);
        medicalService = new MedicalServiceImpl(patientInfoRepository, alertService);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "60, 120, 1",
            "120, 80, 0"
    })
    public void testCheckBloodPressure_sendsAlert(int currentHighBp, int currentLowBp, int times) {
        BloodPressure normalBloodPressure = new BloodPressure(120, 80);
        HealthInfo healthInfo = new HealthInfo(new BigDecimal("36.65"), normalBloodPressure);
        PatientInfo patientInfo = new PatientInfo(id, "Ivan", "Petrov", LocalDate.of(1980, 11, 26), healthInfo);

        BloodPressure currentBloodPressure = new BloodPressure(currentHighBp, currentLowBp);
        Mockito.when(patientInfoRepository.getById(id)).thenReturn(patientInfo);

        medicalService.checkBloodPressure(id, currentBloodPressure);
        Mockito.verify(alertService, Mockito.times(times)).send(expectedMsg);
    }

    @Test
    public void testCheckBloodPressure_validateAlertMsg() {
        BloodPressure normalBloodPressure = new BloodPressure(120, 80);
        HealthInfo healthInfo = new HealthInfo(new BigDecimal("36.65"), normalBloodPressure);
        PatientInfo patientInfo = new PatientInfo(id, "Ivan", "Petrov", LocalDate.of(1980, 11, 26), healthInfo);

        BloodPressure currentBloodPressure = new BloodPressure(60, 120);
        Mockito.when(patientInfoRepository.getById(id)).thenReturn(patientInfo);
        medicalService.checkBloodPressure(id, currentBloodPressure);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(alertService).send(captor.capture());

        Assertions.assertEquals(expectedMsg, captor.getValue());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "35, 1",
            "36.8, 0"
    })
    public void testCheckTemperature_sendsAlert(BigDecimal currentTemperature, int times) {
        BigDecimal normalTemperature = new BigDecimal("36.65");
        HealthInfo healthInfo = new HealthInfo(normalTemperature, new BloodPressure(120, 80));
        PatientInfo patientInfo = new PatientInfo(id, "Ivan", "Petrov", LocalDate.of(1980, 11, 26), healthInfo);

        Mockito.when(patientInfoRepository.getById(id)).thenReturn(patientInfo);

        medicalService.checkTemperature(id, currentTemperature);
        Mockito.verify(alertService, Mockito.times(times)).send(expectedMsg);
    }

    @Test
    public void testCheckTemperature_validateAlertMsg() {
        BigDecimal normalTemperature = new BigDecimal("36.65");
        HealthInfo healthInfo = new HealthInfo(normalTemperature, new BloodPressure(120, 80));
        PatientInfo patientInfo = new PatientInfo(id, "Ivan", "Petrov", LocalDate.of(1980, 11, 26), healthInfo);

        BigDecimal currentTemperature = new BigDecimal("35");
        Mockito.when(patientInfoRepository.getById(id)).thenReturn(patientInfo);
        medicalService.checkTemperature(id, currentTemperature);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(alertService).send(captor.capture());

        Assertions.assertEquals(expectedMsg, captor.getValue());
    }
}