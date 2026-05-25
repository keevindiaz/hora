var DAYS   = ['Dom','Lun','Mar','Mie','Jue','Vie','Sab'];
var MONTHS = ['enero','febrero','marzo','abril','mayo','junio',
              'julio','agosto','septiembre','octubre','noviembre','diciembre'];

var DESC = {
  'Sunny':'Soleado','Clear':'Despejado','Clear ':'Despejado',
  'Partly cloudy':'Parc. nublado','Partly Cloudy':'Parc. nublado','Partly Cloudy ':'Parc. nublado',
  'Cloudy':'Nublado','Cloudy ':'Nublado','Overcast':'Cubierto',
  'Mist':'Neblina','Fog':'Niebla',
  'Light rain':'Lluvia leve','Moderate rain':'Lluvia moderada','Heavy rain':'Lluvia intensa',
  'Light drizzle':'Llovizna leve','Drizzle':'Llovizna',
  'Light snow':'Nieve leve','Moderate snow':'Nieve moderada','Heavy snow':'Nieve intensa',
  'Thundery outbreaks possible':'Tormenta posible','Patchy rain nearby':'Lluvia cercana',
  'Light rain shower':'Chubascos leves','Moderate or heavy rain shower':'Chubascos',
  'Torrential rain shower':'Lluvia torrencial','Thunder':'Tormenta',
  'Patchy light rain':'Lluvia leve','Blowing snow':'Nieve con viento','Blizzard':'Ventisca'
};

function tr(s){ return DESC[s] || (s ? s.trim() : 'Variable'); }
function pad(n){ return n < 10 ? '0'+n : ''+n; }

/* --- Modo día/noche --- */
try { if(localStorage.getItem('mode') === 'day') document.body.classList.add('day'); } catch(e){}
function toggleMode(){
  document.body.classList.toggle('day');
  try { localStorage.setItem('mode', document.body.classList.contains('day') ? 'day' : 'night'); } catch(e){}
}

/* --- Días --- */
function renderDays(){
  var today = new Date().getDay();
  var row = document.getElementById('days-row');
  row.innerHTML = '';
  for(var i = 0; i < 7; i++){
    var s = document.createElement('span');
    if(i === today) s.className = 'active';
    s.textContent = DAYS[i];
    row.appendChild(s);
  }
}

/* --- Reloj --- */
function updateClock(){
  var n = new Date();
  document.getElementById('time').textContent = pad(n.getHours()) + ':' + pad(n.getMinutes());
  document.getElementById('fecha').textContent =
    DAYS[n.getDay()] + ', ' + n.getDate() + ' de ' + MONTHS[n.getMonth()] + ' ' + n.getFullYear();
}

/* --- Clima --- */
function getWeather(){
  var x = new XMLHttpRequest();
  x.open('GET', 'clima.json?t=' + new Date().getTime(), true);
  x.timeout = 10000;

  x.onreadystatechange = function(){
    if(x.readyState !== 4) return;

    if(x.status === 200){
      try{
        var d  = JSON.parse(x.responseText);
        var cc = d.current_condition[0];
        var wd = d.weather[0];

        var temp = Math.round(cc.temp_C);
        var tmax = Math.round(wd.maxtempC);
        var tmin = Math.round(wd.mintempC);
        var desc = tr(cc.weatherDesc[0].value);
        var wind = Math.round(cc.windspeedKmph);

        document.getElementById('weather-block').innerHTML =
          '<div class="weather-temp">' +
            '<span>' + temp + '°C</span>' +
            '<span style="font-size:0.5em;color:#555">&#8593;' + tmax + '° &nbsp; &#8595;' + tmin + '°</span>' +
          '</div>' +
          '<div>' +
            '<div class="weather-desc">' + desc + '</div>' +
            '<div class="weather-detail">Viento <span>' + wind + ' km/h</span></div>' +
          '</div>';

      } catch(e){
        document.getElementById('weather-block').innerHTML = '<div class="msg">error al leer clima</div>';
      }
    } else {
      document.getElementById('weather-block').innerHTML = '<div class="msg">sin clima</div>';
    }
  };

  x.ontimeout = function(){ document.getElementById('weather-block').innerHTML = '<div class="msg">timeout</div>'; };
  x.onerror   = function(){ document.getElementById('weather-block').innerHTML = '<div class="msg">sin conexion</div>'; };

  try{ x.send(); } catch(e){}
}

renderDays();
updateClock();
getWeather();

setInterval(updateClock, 1000);
setInterval(renderDays, 60000);
setInterval(getWeather, 600000);
