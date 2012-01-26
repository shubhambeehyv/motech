package org.motechproject.adherence.service;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.adherence.dao.AllAdherenceLogs;
import org.motechproject.adherence.domain.AdherenceLog;
import org.motechproject.adherence.domain.ErrorFunction;
import org.motechproject.util.DateUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AdherenceServiceTest extends BaseUnitTest {

    @Mock
    private AllAdherenceLogs allAdherenceLogs;
    private AdherenceService adherenceService;
    private String externalId;
    private String conceptId;

    @Before
    public void setUp() {
        initMocks(this);
        adherenceService = new AdherenceService(allAdherenceLogs);
        externalId = "externalId";
        conceptId = "conceptId";
    }

    @Test
    public void shouldStartRecordingAdherence() {
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(null);

        adherenceService.recordUnitAdherence(externalId, conceptId, true, new ErrorFunction(1, 1), null);
        ArgumentCaptor<AdherenceLog> logCapture = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs).insert(logCapture.capture());
        assertEquals(1, logCapture.getValue().getDosesTaken());
        assertEquals(conceptId, logCapture.getValue().getConceptId());
        assertEquals(1, logCapture.getValue().getTotalDoses());
    }

    @Test
    public void shouldRecordUnitAdherence() {
        LocalDate today = DateUtil.today();
        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, today);
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(1);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        adherenceService.recordUnitAdherence(externalId, conceptId, true, new ErrorFunction(1, 1), null);
        ArgumentCaptor<AdherenceLog> logCapture = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs).insert(logCapture.capture());
        assertEquals(2, logCapture.getValue().getDosesTaken());
        assertEquals(2, logCapture.getValue().getTotalDoses());
    }

    @Test
    public void shouldRecordUnitNotTaken() {
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(null);

        adherenceService.recordUnitAdherence(externalId, conceptId, false, new ErrorFunction(1, 1), null);
        ArgumentCaptor<AdherenceLog> logCapture = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs).insert(logCapture.capture());
        assertEquals(0, logCapture.getValue().getDosesTaken());
        assertEquals(1, logCapture.getValue().getTotalDoses());
    }

    @Test
    public void shouldCorrectErrorWhenRecordingAdherence() {
        DateTime now = new DateTime(2011, 12, 2, 10, 0, 0, 0);
        mockTime(now);

        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, now.toLocalDate());
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(2);
        existingLog.setFromDate(now.toLocalDate().minusDays(2));
        existingLog.setToDate(now.toLocalDate().minusDays(2));

        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        adherenceService.recordUnitAdherence(externalId, conceptId, true, new ErrorFunction(0, 1), null);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs, times(2)).insert(logCaptor.capture());
        List<AdherenceLog> allLogs = logCaptor.getAllValues();
        assertEquals(1, allLogs.get(0).getDosesTaken());
        assertEquals(3, allLogs.get(0).getTotalDoses());
        assertEquals(2, allLogs.get(1).getDosesTaken());
        assertEquals(4, allLogs.get(1).getTotalDoses());
    }

    @Test
    public void shouldRecordMetaInformationWhenRecordingAdherence() {
        Map<String, Object> meta = new HashMap<String, Object>() {{
            put("label", "value");
        }};
        adherenceService.recordUnitAdherence(externalId, conceptId, true, new ErrorFunction(0, 0), meta);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs).insert(logCaptor.capture());
        assertEquals(meta, logCaptor.getValue().getMeta());
    }

    @Test
    public void shouldRecordMetaInformationWhenRecordingAdherenceOverDateRange() {
        LocalDate today = DateUtil.today();
        Map<String, Object> meta = new HashMap<String, Object>() {{
            put("label", "value");
        }};
        adherenceService.recordAdherence(externalId, conceptId, 1, 2, today.minusDays(1), today, new ErrorFunction(0, 0), meta);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs).insert(logCaptor.capture());
        assertEquals(meta, logCaptor.getValue().getMeta());
    }

    @Test
    public void shouldRecordAdherenceBetweenARange() {
        LocalDate fromDate = DateUtil.newDate(2011, 12, 1);
        LocalDate toDate = DateUtil.newDate(2011, 12, 31);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(null);

        adherenceService.recordAdherence(externalId, null, 1, 1, fromDate, toDate, new ErrorFunction(0, 0), null);
        ArgumentCaptor<AdherenceLog> logCapture = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs).insert(logCapture.capture());
        assertEquals(fromDate, logCapture.getValue().getFromDate());
        assertEquals(toDate, logCapture.getValue().getToDate());
    }

    @Test
    public void shouldCorrectErrorWhenRecordingAdherenceBetweenARange() {
        DateTime now = new DateTime(2011, 12, 2, 10, 0, 0, 0);
        mockTime(now);

        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, now.toLocalDate());
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(2);
        existingLog.setFromDate(now.toLocalDate().minusDays(2));
        existingLog.setToDate(now.toLocalDate().minusDays(2));

        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        adherenceService.recordAdherence(externalId, conceptId, 1, 1, now.toLocalDate(), now.toLocalDate(), new ErrorFunction(0, 1), null);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs, times(2)).insert(logCaptor.capture());
        List<AdherenceLog> allLogs = logCaptor.getAllValues();
        assertEquals(1, allLogs.get(0).getDosesTaken());
        assertEquals(3, allLogs.get(0).getTotalDoses());
        assertEquals(2, allLogs.get(1).getDosesTaken());
        assertEquals(4, allLogs.get(1).getTotalDoses());
    }

    @Test
    public void shouldSaveMetaWhenCorrectingError() {
        DateTime now = new DateTime(2011, 12, 2, 10, 0, 0, 0);
        mockTime(now);

        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, now.toLocalDate());
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(2);
        existingLog.setFromDate(now.toLocalDate().minusDays(2));
        existingLog.setToDate(now.toLocalDate().minusDays(2));

        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        adherenceService.recordAdherence(externalId, conceptId, 1, 1, now.toLocalDate(), now.toLocalDate(), new ErrorFunction(0, 1), null);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs, times(2)).insert(logCaptor.capture());
        List<AdherenceLog> allLogs = logCaptor.getAllValues();
        assertEquals(true, allLogs.get(0).getMeta().get(AdherenceService.ERROR_CORRECTION));
    }

    @Test
    public void shouldReportRunningAverageAdherence() {
        LocalDate today = DateUtil.today();
        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, today);
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(2);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        assertEquals(0.5, adherenceService.getRunningAverageAdherence(externalId, (String) conceptId));
    }

    @Test
    public void shouldReportRunningAverageAdherenceOnGivenDate() {
        LocalDate today = DateUtil.today();
        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, today);
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(2);
        LocalDate date = DateUtil.newDate(2011, 12, 1);
        when(allAdherenceLogs.findByDate(externalId, conceptId, date)).thenReturn(existingLog);

        assertEquals(0.5, adherenceService.getRunningAverageAdherence(externalId, conceptId, date));
    }

    @Test
    public void shouldReportDeltaAdherence() {
        LocalDate today = DateUtil.today();
        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, today);
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(2);
        existingLog.setDeltaDosesTaken(1);
        existingLog.setDeltaTotalDoses(4);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        assertEquals(0.25, adherenceService.getDeltaAdherence(externalId, conceptId));
    }

    @Test
    public void shouldReportDeltaAdherenceOverDateRange() {
        LocalDate today = DateUtil.today();
        AdherenceLog log = AdherenceLog.create(externalId, conceptId, today);
        log.setDeltaDosesTaken(1);
        log.setDeltaTotalDoses(1);
        AdherenceLog secondLog = AdherenceLog.create(externalId, conceptId, today);
        secondLog.setDeltaDosesTaken(0);
        secondLog.setDeltaTotalDoses(1);

        LocalDate fromDate = DateUtil.newDate(2011, 12, 1);
        LocalDate toDate = DateUtil.newDate(2011, 12, 31);

        when(allAdherenceLogs.findLogsBetween(externalId, conceptId, fromDate, toDate)).thenReturn(Arrays.asList(log, secondLog));
        assertEquals(0.5, adherenceService.getDeltaAdherence(externalId, conceptId, fromDate, toDate));
    }

    @Test
    public void shouldUpdateLatestAdherenceForPositiveChangeInDeltas() {
        LocalDate today = DateUtil.today();
        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, today);
        existingLog.setDosesTaken(1);
        existingLog.setTotalDoses(2);
        existingLog.setDeltaDosesTaken(1);
        existingLog.setDeltaTotalDoses(2);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        adherenceService.updateLatestAdherence(externalId, conceptId, 3, 4);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs, times(1)).update(logCaptor.capture());
        AdherenceLog allLog = logCaptor.getValue();
        assertEquals(3, allLog.getDosesTaken());
        assertEquals(4, allLog.getTotalDoses());
        assertEquals(3, allLog.getDosesTaken());
        assertEquals(4, allLog.getTotalDoses());
    }

    @Test
    public void shouldUpdateLatestAdherenceForNegativeChangeInDeltas() {
        LocalDate today = DateUtil.today();
        AdherenceLog existingLog = AdherenceLog.create(externalId, conceptId, today);
        existingLog.setDosesTaken(4);
        existingLog.setTotalDoses(5);
        existingLog.setDeltaDosesTaken(3);
        existingLog.setDeltaTotalDoses(4);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(existingLog);

        adherenceService.updateLatestAdherence(externalId, conceptId, 2, 3);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs, times(1)).update(logCaptor.capture());
        AdherenceLog allLog = logCaptor.getValue();
        assertEquals(3, allLog.getDosesTaken());
        assertEquals(4, allLog.getTotalDoses());
        assertEquals(2, allLog.getDeltaDosesTaken());
        assertEquals(3, allLog.getDeltaTotalDoses());
    }

    @Test
    public void shouldFetchDateOfLatestAdherence() {
        LocalDate endDate = DateUtil.today();
        AdherenceLog adherenceLog = AdherenceLog.create(externalId, conceptId, endDate.minusDays(1), endDate);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(adherenceLog);
        assertEquals(endDate, adherenceService.getLatestAdherenceDate(externalId, conceptId));
    }

    @Test
    public void shouldRollbackAdherence() {
        LocalDate logDate = DateUtil.newDate(2011, 1, 2);
        mockTime(DateUtil.newDateTime(logDate, 10, 0, 0));

        AdherenceLog adherenceLog = AdherenceLog.create(externalId, conceptId, logDate);
        adherenceLog.setId("logId");
        List<AdherenceLog> adherenceLogs = Arrays.asList(adherenceLog);
        when(allAdherenceLogs.findLogsBetween(externalId, conceptId, logDate, logDate)).thenReturn(adherenceLogs);
        assertEquals(adherenceLogs.get(0), adherenceService.rollBack(externalId, conceptId, logDate.minusDays(1)).get(0));
        verify(allAdherenceLogs).remove(adherenceLogs.get(0));
    }

    @Test
    public void shouldUpdateLogOnRollbackWhenTillDateCutsIt() {
        LocalDate logStartDate = DateUtil.newDate(2011, 1, 1);
        LocalDate logEndDate = DateUtil.newDate(2011, 1, 31);
        mockTime(DateUtil.newDateTime(logEndDate, 10, 0, 0));

        AdherenceLog adherenceLog = AdherenceLog.create(externalId, conceptId, logStartDate, logEndDate);
        adherenceLog.setId("logId");
        List<AdherenceLog> adherenceLogs = Arrays.asList(adherenceLog);
        when(allAdherenceLogs.findLogsBetween(externalId, conceptId, logStartDate.plusDays(1), logEndDate)).thenReturn(adherenceLogs);
        adherenceService.rollBack(externalId, conceptId, logStartDate.plusDays(1));
        verify(allAdherenceLogs, never()).remove(adherenceLogs.get(0));
    }

    @Test
    public void shouldResetAdherence() {
        adherenceService.reset(externalId, conceptId);
        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs).insert(logCaptor.capture());
        assertEquals(true, logCaptor.getValue().getMeta().get(AdherenceLog.RESET_LOG));
    }

    @Test
    public void shouldNotResetAdherenceWhenLogEndingTodayExists() {
        DateTime now = DateUtil.now();
        mockTime(now);
        LocalDate today = now.toLocalDate();

        AdherenceLog adherenceLog = AdherenceLog.create(externalId, conceptId, today);
        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(adherenceLog);
        adherenceService.reset(externalId, conceptId);

        ArgumentCaptor<AdherenceLog> logCaptor = ArgumentCaptor.forClass(AdherenceLog.class);
        verify(allAdherenceLogs, never()).insert(logCaptor.capture());
    }

    @Test
    public void shouldRemoveResetLogWhenRecordingUnitAdherence() {
        DateTime now = DateUtil.now();
        mockTime(now);
        LocalDate today = now.toLocalDate();

        AdherenceLog adherenceLog = AdherenceLog.create(externalId, conceptId, today);
        adherenceLog.putMeta(AdherenceLog.RESET_LOG, true);

        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(adherenceLog);
        adherenceService.recordUnitAdherence(externalId, conceptId, true, new ErrorFunction(0, 0), null);
        verify(allAdherenceLogs).remove(adherenceLog);
    }

    @Test
    public void shouldRemoveResetLogWhenRecordingAdherence() {
        DateTime now = DateUtil.now();
        mockTime(now);
        LocalDate today = now.toLocalDate();

        AdherenceLog adherenceLog = AdherenceLog.create(externalId, conceptId, today);
        adherenceLog.setId("logId");
        adherenceLog.putMeta(AdherenceLog.RESET_LOG, true);

        when(allAdherenceLogs.findLatestLog(externalId, conceptId)).thenReturn(adherenceLog);
        adherenceService.recordAdherence(externalId, conceptId, 1, 1, today, today.plusDays(7), new ErrorFunction(0, 0), null);
        verify(allAdherenceLogs).remove(adherenceLog);
    }

    @After
    public void tearDown() {
        resetTime();
    }
}
