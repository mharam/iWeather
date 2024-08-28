package com.takaapoo.weatherer.ui.viewModels

//sealed interface WeatherResponseState {
//    data class Success(val weather: Weather): WeatherResponseState
//    data class Error(val exception: Exception): WeatherResponseState
//    data object Loading : WeatherResponseState
//}
//
//@HiltViewModel
//class WeatherViewModel @Inject constructor(
//    private val remoteWeatherRepository: RemoteWeatherRepository,
//    private val localWeatherRepository: LocalWeatherRepository
//) : ViewModel() {
//
//
//
//    val allHourlyWeather = localWeatherRepository.getAllHourlyWeather().stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = null
//    )
//
//    suspend fun getDailyWeather(date: String) = localWeatherRepository.getDailyWeather(date)
//
//}