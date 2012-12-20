package org.ei.drishti.reporting.repository;

import org.ei.drishti.common.monitor.Monitor;
import org.ei.drishti.reporting.domain.*;
import org.ei.drishti.reporting.repository.cache.*;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Date;

import static org.ei.drishti.reporting.domain.ServiceProviderType.ANM;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ServicesProvidedRepositoryTest {
    @Mock
    private IndicatorCacheableRepository indicatorRepository;

    @Mock
    private AllLocationsRepository locationRepository;

    @Mock
    private DatesCacheableRepository datesRepository;

    @Mock
    private AllServiceProvidersRepository serviceProvidersRepository;

    @Mock
    private AllServicesProvidedRepository servicesProvidedRepository;

    @Mock
    private Monitor monitor;

    private ServicesProvidedRepository repository;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        repository = new ServicesProvidedRepository(datesRepository, indicatorRepository, locationRepository, serviceProvidersRepository, servicesProvidedRepository, monitor);
    }

    @Test
    public void shouldSaveReportDataAndUseCachedRepositories() throws Exception {
        String anmIdentifier = "ANM X";
        String externalId = "12345";
        String indicator = "ANC";
        String village = "Bherya";
        String subCenter = "Sub Center";
        Date date = LocalDate.parse("2012-04-04").toDate();
        PHC phc = new PHC(34, "PHC X", "PHC");

        ServiceProvider serviceProvider = new ServiceProvider(2, 2, ANM);
        Dates dates = new Dates(2, date);
        Indicator fetchedIndicator = new Indicator(2, indicator);
        Location location = new Location(2, village, subCenter, phc, "taluka", "mysore", "karnataka");
        when(serviceProvidersRepository.fetchBy("ANM X", ANM)).thenReturn(serviceProvider);
        when(datesRepository.fetch(new Dates(date))).thenReturn(dates);
        when(indicatorRepository.fetch(new Indicator(indicator))).thenReturn(fetchedIndicator);
        when(locationRepository.fetchBy(village, subCenter, phc.phcIdentifier())).thenReturn(location);

        repository.save(anmIdentifier, "ANM", externalId, indicator, "2012-04-04", village, subCenter, phc.phcIdentifier(), null);
        repository.save(anmIdentifier, "ANM", externalId, indicator, "2012-04-04", village, subCenter, phc.phcIdentifier(), null);

        verifyCallsToReadOnlyCachedRepository(indicatorRepository, new Indicator(indicator));
        verifyCallsToCachedRepository(datesRepository, new Dates(date));
        verify(locationRepository, times(2)).fetchBy(village, subCenter, phc.phcIdentifier());
        verify(serviceProvidersRepository, times(2)).fetchBy(anmIdentifier, ANM);
        verify(servicesProvidedRepository, times(2)).save(serviceProvider, "12345", fetchedIndicator, dates, location);
    }

    @Test
    public void shouldSaveAsPerQuantityIfQuantityIsNotNull() throws Exception {
        String anmIdentifier = "ANM X";
        String externalId = "12345";
        String indicator = "ANC";
        String village = "Bherya";
        String subCenter = "Sub Center";
        Date date = LocalDate.parse("2012-04-04").toDate();
        PHC phc = new PHC(34, "PHC X", "PHC");

        ServiceProvider serviceProvider = new ServiceProvider(2, 2, ANM);
        Dates dates = new Dates(2, date);
        Indicator fetchedIndicator = new Indicator(2, indicator);
        Location location = new Location(2, village, subCenter, phc, "taluka", "mysore", "karnataka");
        when(serviceProvidersRepository.fetchBy("ANM X", ANM)).thenReturn(serviceProvider);
        when(datesRepository.fetch(new Dates(date))).thenReturn(dates);
        when(indicatorRepository.fetch(new Indicator(indicator))).thenReturn(fetchedIndicator);
        when(locationRepository.fetchBy(village, subCenter, phc.phcIdentifier())).thenReturn(location);

        repository.save(anmIdentifier, "ANM", externalId, indicator, "2012-04-04", village, subCenter, phc.phcIdentifier(), "40");

        verify(servicesProvidedRepository, times(40)).save(serviceProvider, "12345", fetchedIndicator, dates, location);
    }

    private <T> void verifyCallsToReadOnlyCachedRepository(ReadOnlyCacheableRepository<T> repo, T object) {
        verify(repo, times(1)).fetch(object);
        verifyNoMoreInteractions(repo);
    }

    private <T> void verifyCallsToCachedRepository(CacheableRepository<T> repo, T object) {
        verify(repo, times(1)).fetch(object);
        verify(repo, times(0)).save(object);
        verifyNoMoreInteractions(repo);
    }
}
