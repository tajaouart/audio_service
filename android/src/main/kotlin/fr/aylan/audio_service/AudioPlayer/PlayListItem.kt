package fr.aylan.audio_service.AudioPlayer


class PlayListItem(
        /**
         * Id of Audio
         */
        var id: Int,
        /**
         * url of Audio
         */
        var url: String?,
        /**
         * title of Audio
         */
        var title: String?, duration: Int) {

    /**
     * duration of Audio
     */
    var  duration: Int? = duration


}
