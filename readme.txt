Para testar o jogo fazer (ver antes readme-game.txt):

ant run-server

Para iniciar o servidor; e depois

ant run-client

Para iniciar n clientes.

============================================================
Nota para programadores:
Devido à manipulação de classes, a compilação tem que ser feita em várias fases. O buildfile do ant já trata disto tudo, mas seguem-se algumas dicas para quem o for alterar ou fazer uma nova aplicação.

(É também de notar que a instrumentação é completamente opcional e independente da plataforma mobihoc; é possível fazer uma aplicação que não a usa e que usa o restante Mobihoc, só dá é mais trabalho. Para ver que código está a ser gerado [especialmente com o ASM], recomendamos um decompiler como o JAD [http://www.kpdus.com/jad.html].)

Numa primeira fase deve ser compilado o mobihoc e as classes da aplicação cliente que estejam anotadas com @Data (targets compile-mobihoc e compile-pass1 no ant).

De seguida, correr o mobihoc.javassist.EntityAnalyser (para gerar os DataUnits) e o mobihoc.asm.EntityMorph (para instrumentar as classes para usar os DataUnits) sobre as classes anotadas com @Data (target gencode).

Compilar o restante código (target compile-pass2).

E finalmente correr o mobihoc.asm.AppMorph para modificar a classe que implementa IMobihocApp (target gencode-pass3).

GlHf!
