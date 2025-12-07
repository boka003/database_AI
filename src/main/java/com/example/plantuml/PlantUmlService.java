@Autowired
private GenericLlmService genericLlmService;

if ("generic".equalsIgnoreCase(selectedProvider)) {
    return genericLlmService.generate(prompt);
}
