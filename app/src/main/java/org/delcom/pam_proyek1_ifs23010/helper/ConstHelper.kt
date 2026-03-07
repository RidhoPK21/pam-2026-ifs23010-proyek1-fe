package org.delcom.pam_proyek1_ifs23010.helper

class ConstHelper {
    // Route Names
    enum class RouteNames(val path: String) {
        AuthLogin(path = "auth/login"),
        AuthRegister(path = "auth/register"),

        Home(path = "home"),

        Profile(path = "profile"),

        // Ubah dari Todos menjadi Events
        Events(path = "events"),
        EventsAdd(path = "events/add"),
        EventsDetail(path = "events/{eventId}"), // Ubah parameter ke eventId
        EventsEdit(path = "events/{eventId}/edit"),
    }
}