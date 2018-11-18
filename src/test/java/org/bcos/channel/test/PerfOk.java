package org.bcos.channel.test;

import org.bcos.channel.client.Service;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.channel.ChannelEthereumService;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.math.BigInteger;
import java.util.ArrayList;

import static java.lang.System.exit;

public class PerfOk {
	public static void main(String[] args) throws Exception {
		try {
			ApplicationContext context = new ClassPathXmlApplicationContext("classpath:applicationContext.xml");
			Service service = context.getBean(Service.class);
			service.run();
			System.out.println("===================================================================");

			ChannelEthereumService channelEthereumService = new ChannelEthereumService();
			channelEthereumService.setChannelService(service);
			channelEthereumService.setTimeout(10000);
			Web3j web3 = Web3j.build(channelEthereumService);
			Thread.sleep(2000);
		//	ToolConf toolConf=context.getBean(ToolConf.class);
			Credentials credentials = Credentials.create("b83261efa42895c38c6c2364ca878f43e77f3cddbc922bf57d0d48070f79feb6");
            int threadNum = 500;
            if( args.length >= 1 ) {
                threadNum = Integer.parseInt(args[0]);
                System.out.println("thread_num:"  + threadNum);
            }

			if (credentials != null) {
				System.out.println("####create credential succ, begin deploy contract");
				java.math.BigInteger gasPrice = new BigInteger("300000000");
				java.math.BigInteger gasLimit = new BigInteger("300000000");
				java.math.BigInteger initialWeiValue = new BigInteger("0");
				final Ok okDemo = Ok.deploy(web3, credentials, gasPrice, gasLimit, initialWeiValue).send();
				if (okDemo != null) {
                        ArrayList<Thread> threadArray = new ArrayList<Thread>();
                        for(int threadIndex = 0; threadIndex < threadNum; threadIndex++ ) {
                        threadArray.add(new Thread("" + threadIndex) {
                            public void run() {
                            while(true){
                                    try {
							            //System.out.println("####contract address is: " + okDemo.getContractAddress());
    							        TransactionReceipt receipt = okDemo.trans(new BigInteger("4")).send();
    	    						    System.out.println("###callback trans success");
    	    	    					BigInteger toBalance = okDemo.get().send();
	    	    	    				System.out.println("============to balance:" + toBalance.intValue());
		    				            Thread.sleep(1000); //sleep 1s
                                    } catch(Exception e) {
                                        System.out.println("callback trans failed, error message:" + e.getMessage());
                                    }
                                }
                            }
                            });
                        threadArray.get(threadIndex).start();
                     }
                     //join all theads
                     for(int threadIndex = 0; threadIndex < threadNum; threadIndex++)
                         threadArray.get(threadIndex).join();
				} else {
					System.out.println("deploy Ok contract failed");
					exit(1);
				}
			} else {
				System.out.println("create Credentials failed");
				exit(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Execute testok failed");
		}
	}

}