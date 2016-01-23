package it.uspread.core.service

import grails.transaction.Transactional
import it.uspread.core.domain.Setting

/**
 * Service des paramètres de l'application
 */
@Transactional
class SettingService {

    /**
     * Les paramètres de l'application
     * @return
     */
    Setting getSetting() {
        return Setting.first()
    }
}
