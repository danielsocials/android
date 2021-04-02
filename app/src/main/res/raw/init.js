console.log('Onekey web3 provider init')

var config = {
    address: "%1$s",
    rpcUrl: "%2$s",
    chainId: "%3$s",
    debug: "%4$s"
};

window.trustwallet.customMethodMessage = {
    personalEcRecover: {
        postMessage: (message) => {
            provider.sendResponse(message.id, message.object.data);
        }
    },
    signMessageHash: {
        postMessage: (message) => {
            debugPrint('signMessage', message.object.data)
            const id = message.id;
            const messageHex = message.object.data;
            const payload = message.object.params;
            onekeyJsCall.signMessageHex(id, messageHex, payload);
        }
    }
}

const provider = new window.trustwallet.Provider(config);
provider.isDebug = config.debug == 'true';
window.ethereum = provider;
window.web3 = new window.trustwallet.Web3(provider);
window.web3.eth.defaultAccount = config.address;

window.chrome = {webstore: {}};

function debugPrint(...args) {
    if (provider.isDebug) {
        console.log(args)
    }
}

function executeCallback(id, error, result) {
    if (error == null) {
        debugPrint('init.js send Response', id, JSON.stringify(result))
        provider.sendResponse(id, result)
    } else {
        debugPrint('init.js send Error', id, error)
        provider.sendError(id, error)
    }
}

window.web3.setProvider = function () {
    console.debug('Onekey Wallet - overrode web3.setProvider')
}

window.webkit = {
    messageHandlers: {
        signTransaction: {
            postMessage: (message) => {
                debugPrint('init.js sign Transaction', JSON.stringify(message))

                const id = message.id
                const tx = message.object

                var gasLimit = tx.gasLimit || tx.gas || null;
                var gasPrice = tx.gasPrice || null;
                var data = tx.data || null;
                var nonce = tx.nonce || -1;
                onekeyJsCall.signTransaction(message.id, tx.to || null, tx.value, nonce, gasLimit, gasPrice, data);
            }
        },
        requestAccounts: {
            postMessage: (message) => {
                debugPrint('init.js request accounts', JSON.stringify(message))
                executeCallback(message.id, null, [config.address])
            }
        },
        addEthereumChain: {
            postMessage: (message) => {
                debugPrint('init.js add ethereum chain', JSON.stringify(message))
                onekeyJsCall.addEthereumChain(message.id, JSON.stringify(message.object))
            }
        }
    }
};

console.log('Onekey web3 provider init done')
