🔴 Kritischer Bug im Exception Handling
In GlobalExceptionHandler.java:

1 @ExceptionHandler(Exception.class)
2 public ResponseEntity<ErrorResponseDTO> handleOtherExceptions(ApiException ex) { // <-- HIER
3     return buildError(ex.getErrorCode(), HttpStatus.INTERNAL_SERVER_ERROR);
4 }
Du annotierst Exception.class, erwartest aber im Parameter eine ApiException. Wenn eine "normale" Exception (z.B. NullPointerException) geworfen wird, knallt es hier mit einer ClassCastException. Das bricht dein
gesamtes Error-Handling.

🟠 Sicherheitsrisiko & Unsaubere Multi-Tenancy
Die Mandantentrennung (Multi-Tenancy) erfolgt aktuell manuell in den Repositories (z.B. findAllByTenantIdAndStatus).
* Risiko: Ein Entwickler vergisst einmal die tenantId in einem neuen Query, und Mandant A sieht die Daten von Mandant B (Data Leak).
* Lösung: Nutze Hibernate Filter (@Filter / @FilterDef) oder Entity Listeners, um die tenantId global und automatisiert an jedes Query anzuhängen.

🟡 Inkonsistente Dependency Injection
* In RevenueService.java nutzt du saubere Constructor Injection via @RequiredArgsConstructor.
* In JwtFilter.java und SecurityConfig.java nutzt du Field Injection via @Autowired.
* Empfehlung: Wechsle überall konsequent auf Constructor Injection. Das erleichtert das Testen und macht Abhängigkeiten explizit.