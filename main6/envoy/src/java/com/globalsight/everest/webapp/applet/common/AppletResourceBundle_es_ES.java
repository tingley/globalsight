/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.applet.common;

import java.util.ListResourceBundle;
import java.io.Serializable;

/**
 * A resource bundle for en_US labesl and message.
 */
public class AppletResourceBundle_es_ES extends ListResourceBundle implements Serializable
{
    public Object[][] getContents()
    {
        return contents;
    }

    static final Object[][] contents = {
        //labels
        {"action", "Acci\u00f3n"},
        {"ACTIVE_STATE_KEY", "ACTIVE_STATE_KEY"},
        {"activityNode", "Figura de actividad"},
        {"andNode", "Figura \u00abY\u00bb"},
        {"andNodeDialog", "Cuadro de di\u00e1logo de figura \u00abY\u00bb"},
        {"andNodeProperties", "Propiedades de la figura \u00abY\u00bb"},
        {"arrow", "Flecha"},
        {"browse", "Examinar..."},
        {"close", "Cerrar" },
        {"COMPLETE_STATE_KEY", "COMPLETE_STATE_KEY"},
        {"Cancel", "Cancelar"},
        {"conditionNode", "Figura condicional"},
        {"conditionalNodeProperties", "Propiedades de la figura condicional"},
        {"description", "Descripci\u00f3n:"},
        {"decisions", "Decisiones"},
        {"docTitle", "T\u00edtulo del documento"},
        {"down", "Abajo"},
        {"emailAddress", "Direcci\u00f3n electr\u00f3nica:"},
        {"enterScriptName", "Ponga nombre al archivo de comandos"},
        {"epilogue", "Ep\u00edlogo:"},
        {"EVENT_PENDING_KEY", "EVENT_PENDING_KEY"},
        {"exit" , "Salir"},
        {"exitNode", "Figura \u00abSalir\u00bb"},
        {"file", "Archivo"},
        {"general", "General"},
        {"if", "si"},
        {"INACTIVE_STATE_KEY", "INACTIVE_STATE_KEY"},
        {"is", "es"},
        {"name", "Nombre:"},
        {"notification", "Notificaci\u00f3n"},
        {"OK", "Aceptar"},
        {"OK_KEY", "OK_KEY"},
        {"orNode", "Figura \u00abO\u00bb"},
        {"orNodeDialog", "Cuadro de di\u00e1logo de la figura \u00abO\u00bb"},
        {"orNodeProperties", "Propiedades de la figura \u00abO\u00bb"},
        {"path", "Ruta"},
        {"pointer", "Punta de flecha"},
        {"prologue", "Pr\u00f3logo:"},
        {"properties" , "Propiedades"},
        {"save", "Guardar"},
        {"saveNode", "Figura \u00abGuardar\u00bb"},
        {"script", "Archivo de comandos:"},
        {"scripting", "Creaci\u00f3n de archivo de comandos"},
        {"selectDmsDir", "Elija el directorio DMS predeterminado"},
        {"selectScript", "Elija el archivo de comandos..."},
        {"start" , "Comienzo"},
        {"startNodeProperties", "Propiedades de la figura \u00abComienzo\u00bb"},
        {"then", "entonces"},
        {"title", "T\u00edtulo"},
        {"togglesEmailNotification", "Activa/desactiva la notificaci\u00f3n por correo electr\u00f3nico"},
        {"togglesNotification", "Activar/desactivar notificaci\u00f3n"},
        {"up", "Arriba"},

        //messages
        {"msg_arrowEmptyName","P\u00f3ngale nombre a la flecha."},
        {"msg_arrowMaxLength","La flecha no puede tener m\u00e1s de 30 caracteres.\nP\u00f3ngale otro nombre."},
        {"msg_arrowWithPendingEvents","Esta flecha tiene sucesos pendientes. \u00bfQuiere continuar?"},
        {"msg_conditionToCondition","<You cannot have a condition node\n connecting to another condition node.>"},
        {"msg_dataConversionError","Error de conversi\u00f3n de datos"},
        {"msg_eventPendingWarning","Advertencia sobre sucesos pendientes"},
        {"msg_exitNode","Un proceso de producci\u00f3n debe tener, como m\u00ednimo, una figura de actividad y otra figura de salida."},
        {"msg_fileExtension", "Error en la extensi\u00f3n de fichero"},
        {"msg_incorrectDataType","Los datos del valor no coinciden con los tipos de datos definidos."},
        {"msg_invalidWorkflow","El proceso de producci\u00f3n no es correcto. Confirme lo siguiente:\n 1. Todas las figuras tienen flechas de salida o entrada;\n 2. Todas las figuras contienen la informaci\u00f3n necesaria. \n"},
        {"msg_noDirSelection", "La selecci\u00f3n no puede ser un directorio."},
        {"msg_noDupArrowError", "No puede duplicar los r\u00f3tulos de las flechas de esta figura.\n Se ha restaurado el antiguo nombre de la flecha.\n C\u00e1mbielo si quiere, pero no lo duplique."},
        {"msg_pathField", "El campo de ruta no puede quedarse vac\u00edo."},
        {"msg_pathFieldError", "Error en el campo de ruta"},
        {"msg_selectionError", "Error en la selecci\u00f3n de ficheros"},
        {"msg_twoArrows","No puede hacer que dos flechas\nse conecten a los mismos nodos"}
    };
}
