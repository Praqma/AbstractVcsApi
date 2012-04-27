package net.praqma.ava.model.extensions;

public class PullListenerImpl extends PullListener {

	@Override
	public void onPrePull() {
		System.out.println("HERE!");
		
	}

	@Override
	public void onPostPull() {
		System.out.println("Whoa, POST!");
		
	}

}
