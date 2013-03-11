package org.motechproject.openmrs.services;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.mrs.domain.Facility;
import org.motechproject.mrs.domain.Observation;
import org.motechproject.openmrs.model.OpenMRSEncounter;
import org.motechproject.openmrs.model.OpenMRSFacility;
import org.motechproject.openmrs.model.OpenMRSObservation;
import org.motechproject.openmrs.model.OpenMRSPatient;
import org.motechproject.openmrs.model.OpenMRSPerson;
import org.motechproject.openmrs.model.OpenMRSProvider;
import org.motechproject.openmrs.model.OpenMRSUser;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.EncounterService;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


public class OpenMRSEncounterAdapterTest {
    OpenMRSEncounterAdapter encounterAdapter;
    @Mock
    private OpenMRSUserAdapter mockOpenMrsUserAdapter;
    @Mock
    private OpenMRSFacilityAdapter mockOpenMrsFacilityAdapter;
    @Mock
    private OpenMRSPatientAdapter mockOpenMrsPatientAdapter;
    @Mock
    private OpenMRSObservationAdapter mockOpenMrsObservationAdapter;
    @Mock
    private EncounterService mockEncounterService;
    @Mock
    private OpenMRSPersonAdapter mockOpenMRSPersonAdapter;

    @Before
    public void setUp() {
        initMocks(this);
        encounterAdapter = new OpenMRSEncounterAdapter(mockEncounterService, mockOpenMrsUserAdapter, mockOpenMrsFacilityAdapter, mockOpenMrsPatientAdapter, mockOpenMrsObservationAdapter, mockOpenMRSPersonAdapter);
    }

    @Test
    public void shouldConvertMrsEncounterToOpenMrsEncounter() {
        String staffId = "333";
        String facilityId = "99";
        String patientId = "199";
        String providerId = "100";
        OpenMRSUser staff = new OpenMRSUser().id(staffId);
        Facility facility = new OpenMRSFacility(facilityId);
        org.motechproject.mrs.domain.Patient patient = new OpenMRSPatient(patientId);
        Set<OpenMRSObservation> observations = Collections.EMPTY_SET;

        String encounterType = "encounterType";
        String encounterId = "100";

        Date encounterDate = Calendar.getInstance().getTime();
        OpenMRSPerson person = new OpenMRSPerson().id(providerId);
        OpenMRSProvider provider = new OpenMRSProvider(person);
        provider.setProviderId(providerId);
        OpenMRSEncounter mrsEncounter = new OpenMRSEncounter.MRSEncounterBuilder().withId(encounterId).withProvider(provider).withCreator(staff)
                .withFacility(facility).withDate(encounterDate).withPatient((OpenMRSPatient) patient).withObservations(observations)
                .withEncounterType(encounterType).build();

        Location expectedLocation = mock(Location.class);
        when(mockOpenMrsFacilityAdapter.getLocation(facilityId)).thenReturn(expectedLocation);

        org.openmrs.Patient expectedPatient = mock(org.openmrs.Patient.class);
        when(mockOpenMrsPatientAdapter.getOpenMrsPatient(patientId)).thenReturn(expectedPatient);

        org.openmrs.User expectedCreator = mock(org.openmrs.User.class);
        Person expectedPerson = mock(Person.class);
        when(mockOpenMrsUserAdapter.getOpenMrsUserById(staffId)).thenReturn(expectedCreator);
        when(expectedCreator.getPerson()).thenReturn(expectedPerson);

        Person expectedProvider = mock(Person.class);
        when(mockOpenMRSPersonAdapter.getPersonById(providerId)).thenReturn(expectedProvider);

        EncounterType expectedEncounterType = mock(EncounterType.class);
        when(mockEncounterService.getEncounterType(encounterType)).thenReturn(expectedEncounterType);

        Set<Obs> expectedObservations = new HashSet<Obs>();
        Encounter expectedEncounter = mock(Encounter.class);

        when(mockOpenMrsObservationAdapter.createOpenMRSObservationsForEncounter(observations, expectedEncounter, expectedPatient, expectedLocation, expectedCreator)).thenReturn(expectedObservations);

        Encounter returnedEncounter = encounterAdapter.mrsToOpenMRSEncounter(mrsEncounter);

        assertThat(returnedEncounter.getLocation(), is(equalTo(expectedLocation)));
        assertThat(returnedEncounter.getPatient(), is(equalTo(expectedPatient)));
        assertThat(returnedEncounter.getCreator(), is(equalTo(expectedCreator)));
        assertThat(returnedEncounter.getProvider(), is(equalTo(expectedProvider)));
        assertThat(returnedEncounter.getEncounterDatetime(), is(equalTo(encounterDate)));
        assertThat(returnedEncounter.getEncounterType(), is(equalTo(expectedEncounterType)));
        assertThat(returnedEncounter.getObs(), is(equalTo(expectedObservations)));
    }

    @Test
    public void shouldConvertOpenMRSEncounterToMRSEncounter() {

        String encounterTypeName = "ANCVisit";
        EncounterType openMrsEncounterType = new EncounterType(encounterTypeName, "Ghana Antenatal Care (ANC) Visit");
        HashSet<Obs> openMrsObservations = new HashSet<>();
        org.openmrs.Patient mockOpenMRSPatient = mock(org.openmrs.Patient.class);
        org.openmrs.User mockOpenMRSUser = mock(org.openmrs.User.class);
        Location mockLocation = mock(Location.class);
        int encounterId = 12;
        Date encounterDate = new LocalDate(2011, 12, 12).toDate();
        org.motechproject.mrs.domain.Facility mrsfacility = mock(OpenMRSFacility.class);
        org.motechproject.mrs.domain.Patient mrspatient = mock(OpenMRSPatient.class);


        Set<Observation> mrsObservations = new HashSet<>();
        mrsObservations.add(mock(OpenMRSObservation.class));

        Integer providerId = 1;
        String systemId = "admin";
        when(mockOpenMRSUser.getSystemId()).thenReturn(systemId);
        Person mockOpenMRSPerson = mock(Person.class);
        when(mockOpenMRSPerson.getId()).thenReturn(providerId);
        Encounter openMrsEncounter = createOpenMRSEncounter(encounterDate, openMrsEncounterType, openMrsObservations, mockOpenMRSPatient, mockOpenMRSUser, mockLocation, encounterId);
        openMrsEncounter.setProvider(mockOpenMRSPerson);
        openMrsEncounter.setCreator(mockOpenMRSUser);
        when(mockOpenMrsFacilityAdapter.convertLocationToFacility(mockLocation)).thenReturn(mrsfacility);
        when(mockOpenMrsPatientAdapter.getMrsPatient(mockOpenMRSPatient)).thenReturn((OpenMRSPatient) mrspatient);
        when(mockOpenMrsObservationAdapter.convertOpenMRSToMRSObservations(openMrsObservations)).thenReturn(mrsObservations);

        OpenMRSEncounter mrsEncounter = encounterAdapter.openmrsToMrsEncounter(openMrsEncounter);

        assertThat(mrsEncounter.getId(), is(equalTo(Integer.toString(encounterId))));
        assertThat(mrsEncounter.getEncounterType(), is(equalTo(encounterTypeName)));
        assertThat(mrsEncounter.getCreator().getSystemId(), is(equalTo(systemId)));
        assertThat(mrsEncounter.getProvider().getProviderId(), is(equalTo(providerId.toString())));
        assertThat(mrsEncounter.getPatient(), is(equalTo(mrspatient)));
        assertThat(mrsEncounter.getDate().toDate(), is(equalTo(encounterDate)));
        assertThat(mrsEncounter.getFacility(), is(equalTo(mrsfacility)));
        //assertThat(mrsEncounter.getObservations(), is(equalTo(mrsObservations)));
    }

    private Encounter createOpenMRSEncounter(Date encounterDate, EncounterType openMrsEncounterType, HashSet<Obs> openMrsObservations, org.openmrs.Patient mockOpenMRSPatient, org.openmrs.User mockOpenMRSUser, Location mockLocation, int encounterId) {
        Encounter openMrsEncounter = new Encounter();
        openMrsEncounter.setId(encounterId);
        openMrsEncounter.setObs(openMrsObservations);
        openMrsEncounter.setEncounterType(openMrsEncounterType);
        openMrsEncounter.setCreator(mockOpenMRSUser);
        openMrsEncounter.setLocation(mockLocation);
        openMrsEncounter.setPatient(mockOpenMRSPatient);
        openMrsEncounter.setEncounterDatetime(encounterDate);
        return openMrsEncounter;
    }

    @Test
    public void shouldSaveAnEncounter() {
        OpenMRSEncounterAdapter encounterAdapterSpy = spy(encounterAdapter);
        Encounter openMrsEncounter = mock(Encounter.class);
        final Date encounterDate = new Date();
        OpenMRSEncounter mrsEncounter = new OpenMRSEncounter.MRSEncounterBuilder().withId("id").withDate(encounterDate)
                .withPatient(new OpenMRSPatient("motechId", null, null)).withObservations(null)
                .withEncounterType(null).build();
        Encounter savedOpenMrsEncounter = mock(Encounter.class);
        OpenMRSEncounter savedMrsEncounter = mock(OpenMRSEncounter.class);
        final Patient patient = mock(Patient.class);

        doReturn(openMrsEncounter).when(encounterAdapterSpy).mrsToOpenMRSEncounter(mrsEncounter);
        when(mockEncounterService.saveEncounter(openMrsEncounter)).thenReturn(savedOpenMrsEncounter);
        doReturn(savedMrsEncounter).when(encounterAdapterSpy).openmrsToMrsEncounter(savedOpenMrsEncounter);

        final EncounterType encounterType = new EncounterType();
        when(mockEncounterService.getEncounterType(anyString())).thenReturn(encounterType);
        when(mockEncounterService.getEncounters(patient, null, encounterDate, encounterDate, null, asList(encounterType), null, false)).thenReturn(null);

        OpenMRSEncounter returnedMRSEncounterAfterSaving = encounterAdapterSpy.createEncounter(mrsEncounter);
        assertThat(returnedMRSEncounterAfterSaving, is(equalTo(savedMrsEncounter)));
    }

    @Test
    public void shouldFetchLatestEncounterForMotechId() {
        String encounterType = "Encounter Type";
        String motechId = "1234567";
        encounterAdapter.getLatestEncounterByPatientMotechId(motechId, encounterType);
        verify(mockEncounterService).getEncountersByPatientIdentifier(motechId);
    }

    @Test
    public void shouldFetchTheLatestEncounterIfThereAreMoreThanOneEncounters() {
        String encounterName = "Encounter Type";
        String motechId = "1234567";
        Encounter encounter1 = new Encounter(1);
        EncounterType encounterType1 = new EncounterType();
        encounterType1.setName(encounterName);
        encounter1.setEncounterType(encounterType1);
        Date encounterDatetime1 = new LocalDate(1999, 11, 2).toDate();
        encounter1.setEncounterDatetime(encounterDatetime1);
        encounter1.setCreator(new User(12));
        encounter1.setProvider(new Person());
        Encounter encounter2 = new Encounter(2);
        encounter2.setEncounterType(encounterType1);
        encounter2.setEncounterDatetime(new LocalDate(1998, 11, 6).toDate());

        when(mockEncounterService.getEncountersByPatientIdentifier(motechId)).thenReturn(Arrays.asList(encounter1, encounter2));
        OpenMRSEncounter actualEncounter = encounterAdapter.getLatestEncounterByPatientMotechId(motechId, encounterName);

        assertThat(actualEncounter.getDate().toDate(), is(equalTo(encounterDatetime1)));
    }

    @Test
    public void shouldReturnNullIfEncounterIsNotFound() {
        final String motechId = "1332";
        final String encounterName = "patientRegistration";
        when(mockEncounterService.getEncountersByPatientIdentifier(motechId)).thenReturn(Collections.<Encounter>emptyList());
        OpenMRSEncounter patientRegistration = encounterAdapter.getLatestEncounterByPatientMotechId(motechId, encounterName);
        assertNull(patientRegistration);
    }
}
