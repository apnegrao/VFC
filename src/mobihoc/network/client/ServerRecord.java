package mobihoc.network.client;

import mobihoc.network.*;

/** Classe ServerRecord.
 * Representa um servidor encontrado na busca.
 **/
public abstract class ServerRecord {

	private boolean isDirect;

	public ServerRecord(boolean isDirect) {
		this.isDirect = isDirect;
	}

	public abstract String toString();
	public abstract NetworkConnection getNetworkConnection();
	
	/** Um ServerRecord directo é um em que não se deve mostrar ao utilizador para escolher (por exemplo, o de uma ligação tcp directa) vs um não-directo, em que o utilizador pode querer escolher (por exemplo, vários jogos detectados na rede) **/
	public boolean isDirect() {
		return isDirect;
	}

}
