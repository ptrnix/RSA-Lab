# RSA Lab

A small Java console tool that shows **why small RSA keys are insecure**.

It plays both sides of the story:

1. **The victim** generates an RSA key and encrypts a text. Only the public key `(n, e)` and the ciphertext are handed over.
2. **The attacker** factors `n`, rebuilds the private key from the factors and decrypts the text.

Two factoring algorithms are implemented and compared side by side: a multi-threaded **brute force** search and **Pollard rho** with Brent's cycle detection.

> **Educational project.** This is textbook RSA without padding and with tiny keys. It is intentionally insecure and must never be used to protect real data.

## How it works

### RSA (`rsa` package)
- `RSA.generatePublicKey(bits)` creates two random primes `p` and `q` and returns `n = p * q` and `e = 65537`. `p`, `q` and the private exponent are discarded on purpose.
- `RSA.encrypt(text, key)` converts the text to UTF-8 bytes and encrypts it block by block (`c = m^e mod n`). A block has to be smaller than `n`, so a small key means many small blocks. Each block gets a leading `0x01` byte, otherwise leading zero bytes would get lost in the `BigInteger` conversion.
- `RSA.privateExponent(key, p)` rebuilds `d` from the public key and one prime factor of `n`.
- `RSA.decrypt(blocks, n, d)` reverses the encryption (`m = c^d mod n`).

### Attacks (`factorization` package)
Both attacks implement the `Factorizer` interface and share their thread handling in `ParallelFactorizer` (template method).

| Algorithm | Idea |
|---|---|
| **Brute force** | The smaller prime factor is at most `sqrt(n)`. The range `[3, sqrt(n)]` is split evenly across all threads, each one tests the odd candidates. |
| **Pollard rho (Brent)** | Walks through the pseudo-random sequence `f(x) = x² + c mod n` and looks for a collision modulo an unknown factor using `gcd`. Needs roughly `n^(1/4)` steps instead of `n^(1/2)`. Every thread starts from a different random point, the first one to succeed wins. |

`ProgressMonitor` logs the number of iterations once per second while an attack is running.

## Requirements
- Java 21
- Maven

## Run
Open the project in IntelliJ and run `App`, or from the command line:

```bash
mvn compile exec:java -Dexec.mainClass=App
```

The program asks for a text (default `Hello World!`) and a key size in bits (default `64`), then runs both attacks one after the other.

Example (shortened):

```
Text to encrypt [Hello World!]:
Key size in bits [64]: 52
Public key: n=2480436390082229 (52 bit), e=65537
Ciphertext: [2026451577189044, 1371798004179285, 1339420971141285]
--- Brute force ---
Factor found (brute force): 48796843
Decrypted: "Hello World!"
--- Pollard rho (Brent) ---
Factor found (Pollard rho, Brent): 48796843
Decrypted: "Hello World!"
```

Brute force gets slow quickly as the key grows, because the search range roughly doubles every two bits. Pollard rho still finishes in a fraction of a second at 80 bits. The more bits you choose, the more obvious the difference gets.

## Project structure

```
src/main/java/
  App.java                      entry point: encrypt, then attack
  rsa/
    RSA.java                    key generation, encryption, decryption
    RsaPublicKey.java           record (n, e)
  factorization/
    Factorizer.java             strategy interface
    ParallelFactorizer.java     thread pool, monitor, result handling
    BruteForceFactorizer.java   splits the search range
    BruteForceWorkerTask.java   tests one range of candidates
    PollardRhoFactorizer.java   starts independent random walks
    PollardRhoWorkerTask.java   Pollard rho with Brent's cycle detection
  monitoring/
    ProgressMonitor.java        prints the iteration count every second
```

## Tech
Java 25, Maven, SLF4J with Log4j2, Lombok