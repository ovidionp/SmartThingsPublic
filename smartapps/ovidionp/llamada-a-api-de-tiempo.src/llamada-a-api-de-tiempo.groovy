/**
 *  Llamada a API de tiempo
 *
 *  Copyright 2018 ovidio navarro
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Llamada a API de tiempo",
    namespace: "ovidionp",
    author: "ovidio navarro",
    description: "consultar el tiempo haciendo una llamada Web desde nuestra aplicaci\u00F3n SmartThing al servidor openweathermap.org a trav\u00E9s de su API. Consultamos el tiempo cada 30 minutos y si la temperatura es menor de 10, encendemos la calefacci\u00F3n",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Comprobar interruptor atendiendo al tiempo que hace: ") {
			input ("elInterruptor","capability.switch",
 					title: "Selecciona interruptor?", required: true, multiple: false)
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
    
}

def initialize(evt) {
	runEvery10Minutes(consultarTiempoJSON);
}

// TODO: implement event handlers

def consultarTiempoJSON(){
    //Construimos la petición HTTP
    def params = [
    uri: "http://api.openweathermap.org/data/2.5/",
    path: "weather",
    contentType: "application/json",
    query: [q:"Málaga", units: "metric",
    		APPID: 'b811614ad352ec866712a6e9439e3462']
    //lat:latitud y lon: longitud  para buscar por Ciudad
    ]
    //Ejecutamos la petición HTTP y consultamos la respuesta
    def temp = 0 //inicializamos la temperatuda a 0
    try {
        httpGet(params) {resp ->
                        log.debug "resp data: ${resp.data}"
                        temp = resp?.data?.main?.temp //guardamos la temperatura del mapa recibido
                        log.debug "temp: ${temp}"
        }
        if (elInterruptor.latestValue("switch") == "off" && temp > 25){
                  log.debug "enciendo interruptor"
                        elInterruptor.on()
        }
    } 
    catch (e) { 
    log.error "error: $e" 
    }
}
