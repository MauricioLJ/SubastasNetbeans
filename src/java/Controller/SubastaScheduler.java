///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package Controller;
//
//import Controller.SubastaController;
//import java.time.LocalDateTime;
//import java.util.Date;
//import java.util.List;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import model.Puja;
//import model.Subasta;
//import servicio.ServicioNotificacion;
//import servicio.ServicioPuja;
//import servicio.ServicioSubasta;
//
///**
// *
// * @author Dariana
// */
//public class SubastaScheduler {
//
//    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//    private ServicioSubasta servicioSubasta = new ServicioSubasta();
//    private final SubastaController subastaController = new SubastaController();
//
//    public SubastaScheduler(ServicioSubasta servicioSubasta, SubastaController subastaController) {
//        this.servicioSubasta = servicioSubasta;
//        //this.subastaController = subastaController;
//    }
//
//    public void iniciar() {
//        scheduler.scheduleAtFixedRate(() -> {
//            try {
//                comprobarSubastasExpiradas();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, 0, 1, TimeUnit.MINUTES); // Revisión cada minuto
//    }
//
//    public void detener() {
//        scheduler.shutdown();
//    }
//
//    private void comprobarSubastasExpiradas() {
//        List<Subasta> subastasActivas = servicioSubasta.listarSubastas();
//        for (Subasta subasta : subastasActivas) {
//            if (subasta.getFechaFin().before(new Date())) {
//                try {
//                    // Declarar la puja más alta como ganadora
//                    ServicioPuja servicioPuja = new ServicioPuja();
//                    List<Puja> pujas = servicioPuja.listaPujasPorSubasta(subasta.getIdSubasta());
//
//                    Puja pujaGanadora = pujas.stream()
//                            .max((p1, p2) -> Double.compare(p1.getMonto(), p2.getMonto()))
//                            .orElse(null);
//
//                    if (pujaGanadora != null) {
//                        subasta.setPujaGanadora(pujaGanadora);
//                    }
//
//                    // Finalizar la subasta
//                    subasta.setEstadoSubasta("Finalizado");
//                    servicioSubasta.actualizarSubasta(subasta);
//
//                    if (pujaGanadora != null) {
//                        subastaController.enviarNotificacionGanador(subasta);
//                    }
//                    subastaController.enviarNotificacionVendedor(subasta);
//
//                    System.out.println("Subasta finalizada correctamente: ID " + subasta.getIdSubasta());
//                } catch (Exception e) {
//                    System.err.println("Error al finalizar la subasta ID " + subasta.getIdSubasta());
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//}
