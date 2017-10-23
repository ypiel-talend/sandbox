public class Resources {
	
	public final static String[] MSCRM2016_C = {"http://13.81.213.2:5555/oumscrm2016/api/data/v8.0", "talend", "R&D_talend_0808", "dmnmscrmc2016c"};
	public final static String[] MSCRM2016_F = {"http://40.112.254.95:8080/oumscrm2016/api/data/v8.0", "talend", "R&D_talend_0808", "tlndmscrmf"};
	public final static String[] MSCRM2016_F3_HTTP = {"http://13.64.235.114:5555/oumscrm2016/api/data/v8.1", "talend", "R&D_talend_0808", "tlndmscrmf"};
	public final static String[] MSCRM2016_G = {"http://104.45.20.183:8080/tlndcomponents/api/data/v8.0", "talend", "R&D_talend_0808", "tlndmscrme"};
    public final static String[] MSCRM2016_TALEND = {"http://crm.dmnmscrm2016f.com:5555/oumscrm2016/api/data/v8.1", "talend", "R&D_talend_0808", "tlndmscrmf"};

	
	public final static String[] CURRENT = MSCRM2016_TALEND;
	
    public final static String SERVICEROOT = CURRENT[0];
    public final static String USERNAME = CURRENT[1];
    public final static String PASSWORD = CURRENT[2];
    public final static String DOMAIN = CURRENT[3];
    public final static String HOST = "tlnd-ypiel";
    
}
