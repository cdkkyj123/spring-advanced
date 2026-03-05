package org.example.expert.client;

import org.example.expert.client.dto.WeatherDto;
import org.example.expert.domain.common.exception.ServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class WeatherClientTest {

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WeatherClient weatherClient;

    @BeforeEach
    void setUp() {
        // RestTemplate 생성 동작 설정
        given(restTemplateBuilder.build()).willReturn(restTemplate);
        weatherClient = new WeatherClient(restTemplateBuilder);
    }

    @Test
    @DisplayName("오늘 날씨 조회 성공")
    public void getTodayWeather_오늘_날씨를_정상적으로_조회한다() {
        // given
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MM-dd"));
        WeatherDto[] weatherArray = {new WeatherDto(today, "Sunny")};
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherArray, HttpStatus.OK);

        // 날씨 데이터 호출 성공
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        // when
        String result = weatherClient.getTodayWeather();

        // then
        assertNotNull(result);
        assertEquals("Sunny", result);
    }

    @Test
    @DisplayName("오늘 날씨 조회 실패 - 외부 API 에러")
    public void getTodayWeather_외부_API_상태코드가_OK가_아니면_에러가_발생한다() {
        // given
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        // 외부 API 호출 실패(500)
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                weatherClient.getTodayWeather()
        );

        // then
        assertTrue(exception.getMessage().contains("날씨 데이터를 가져오는데 실패했습니다."));
    }

    @Test
    @DisplayName("오늘 날씨 조회 실패 - 데이터 누락")
    public void getTodayWeather_날씨_데이터가_없으면_에러가_발생한다() {
        // given
        WeatherDto[] emptyArray = {};
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(emptyArray, HttpStatus.OK);

        // API는 성공했으나 데이터가 비어있음
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                weatherClient.getTodayWeather()
        );

        // then
        assertEquals("날씨 데이터가 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("오늘 날씨 조회 실패 - 오늘 날짜 데이터 없음")
    public void getTodayWeather_오늘_날짜에_해당하는_데이터가_없으면_에러가_발생한다() {
        // given
        WeatherDto[] weatherArray = {new WeatherDto("12-31", "Snow")}; // 오늘이 아닌 날짜
        ResponseEntity<WeatherDto[]> responseEntity = new ResponseEntity<>(weatherArray, HttpStatus.OK);

        // 오늘 날짜와 일치하는 데이터가 없는 배열 반환
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(responseEntity);

        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                weatherClient.getTodayWeather()
        );

        // then
        assertEquals("오늘에 해당하는 날씨 데이터를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("오늘 날씨 조회 실패 - 날씨 데이터 null")
    public void getTodayWeather_날씨_데이터가_null이면_에러가_발생한다() {
        // given
        // 날씨 데이터 null 반환
        given(restTemplate.getForEntity(any(URI.class), eq(WeatherDto[].class)))
                .willReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // when
        ServerException exception = assertThrows(ServerException.class, () ->
                weatherClient.getTodayWeather()
        );

        // then
        assertEquals("날씨 데이터가 없습니다.", exception.getMessage());
    }
}