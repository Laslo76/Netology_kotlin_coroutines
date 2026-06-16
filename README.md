# Домашнее задание к занятию «Coroutines: Scopes, Cancellation, Supervision»
## Вопросы: Cancellation
### Вопрос 1.

#### Функция:
fun main() = runBlocking {
    val job = CoroutineScope(EmptyCoroutineContext).launch {
        launch {
            delay(500)
            println("ok") // <--
        }
        launch {
            delay(500)
            println("ok")
        }
    }
    delay(500)
    job.cancelAndJoin()
}

#### Результат: 
Строка не будет выполненна так-ка закончится срок ожидания родительской корутины 500мс, и она будет
отменена вмести с обеими дочерними корутинами. Если для основной корутины увеличить срок ожидания, к примеру, до
700мс то обе дочерние корутины отработают и выведут свои сообщения.

### Вопрос 2.
'''
Функция:
fun main() = runBlocking {
    val job = CoroutineScope(EmptyCoroutineContext).launch {
        val child = launch {
            delay(500)
            println("ok") // <--
        }
        launch {
            delay(500)
            println("ok")
        }
        delay(100)
        child.cancel()
    }
    delay(100)
    job.join()
}
Результат: Строка не будет выполненна так-ка закончится срок ожидания delay(100) и корутина содержащая целевую 
строку будет отменена. Если задержку перед целевой строкой уменьшить до 50мс то целевая строка успеет
выполниться до отмены содержащей ее корутины.
'''
## Вопросы: Exception Handling
### Вопрос 1.
'''
Функция:
fun main() {
    with(CoroutineScope(EmptyCoroutineContext)) {
        try {
            launch {
                throw Exception("something bad happened")
            }
        } catch (e: Exception) {
            e.printStackTrace() // <--
        }
    }
    Thread.sleep(1000)
}
Результат: Строка не будет выполненна. Блок catch сработает только в том случае, если исключение будет выброшено
прямо внутри блока try. Но в нашем случае исключение выбрасывается внутри асинхронно выполняющейся корутины.
К моменту, когда эта корутина фактически начнет выполняться и упадет, блок try-catch уже давно будет отработан.
'''
### Вопрос 2.
'''
Функция:
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
            coroutineScope {
                throw Exception("something bad happened")
            }
        } catch (e: Exception) {
            e.printStackTrace() // <--
        }
    }
    Thread.sleep(1000)
}
Результат: Строка будет выполненна. Главная задача coroutineScope — дождаться завершения всех корутин,
запущенных внутри нее. В отличие от launch coroutineScope как бы приостонавливает выполнение потока до
завершения всех корутин в нутри нее. Таким образом ее действие будет завершено с исключением и программа
перейдет в блок catch.
'''
### Вопрос 3.
'''
Функция:
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
            supervisorScope {
                throw Exception("something bad happened")
            }
        } catch (e: Exception) {
            e.printStackTrace() // <--
        }
    }
    Thread.sleep(1000)
}
Результат: Строка будет выполненна. Как и coroutineScope supervisorScope передает выше по потоку программы
произошедшие исключения.
'''
### Вопрос 4.
'''
Функция:
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
            coroutineScope {
                launch {
                    delay(500)
                    throw Exception("something bad happened") // <--
                }
                launch {
                    throw Exception("something bad happened")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    Thread.sleep(1000)
}
Результат: Строка не будет выполненна. Первым упадет вторая корутина (она вылняется без задержки) и выше
по потоку программы пойдет именно её исключение.
'''
### Вопрос 5.
'''
Функция:
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
            supervisorScope {
                launch {
                    delay(500)
                    throw Exception("something bad happened") // <--
                }
                launch {
                    throw Exception("something bad happened")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace() // <--
        }
    }
    Thread.sleep(1000)
}
Результат: Обе строки выполняться, а строка e.printStackTrace() выполниться дважды для обеих корутин.
В отличчие от coroutineScope supervisorScope при возникновении исключения в корутине не останавливает
другие корутины. Поэтому выдаст выше по потоку программы оба исключения вначале из второй коррутины
потом из первой.
'''
### Вопрос 6.
'''
Функция:
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        CoroutineScope(EmptyCoroutineContext).launch {
            launch {
                delay(1000)
                println("ok") // <--
            }
            launch {
                delay(500)
                println("ok")
            }
            throw Exception("something bad happened")
        }
    }
    Thread.sleep(1000)
}
Результат: не выполниться. В программе создана иерархия корутин 
Корутина А: CoroutineScope(EmptyCoroutineContext).launch { ... } (самая внешняя).
Корутина Б: CoroutineScope(EmptyCoroutineContext).launch { ... } (запущена внутри А).
Корутина В: launch { delay(1000); println("ok") } (запущена внутри Б).
Корутина Г: launch { delay(500); println("ok") } (запущена внутри Б).
Все корутины запускаются и начинают свою работу. Корутина Г первая достигает своей задержки delay(500).
Она приостанавливается, передавая управление обратно в диспетчер. Следом выполняется код в корутине Б.
Он доходит до строки throw Exception("something bad happened") и падает с исключением завершая все
корутины этого уровня.
'''
### Вопрос 7.
'''
Функция:
fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        CoroutineScope(EmptyCoroutineContext + SupervisorJob()).launch {
            launch {
                delay(1000)
                println("ok") // <--
            }
            launch {
                delay(500)
                println("ok")
            }
            throw Exception("something bad happened")
        }
    }
    Thread.sleep(1000)
}
Результат: не выполниться. . В программе создана иерархия корутин 
Корутина А: CoroutineScope(EmptyCoroutineContext).launch { ... } (самая внешняя).
Корутина Б: CoroutineScope(EmptyCoroutineContext + SupervisorJob()).launch { ... } (запущена внутри А).
Корутина В: launch { delay(1000); println("ok") } (запущена внутри Б).
Корутина Г: launch { delay(500); println("ok") } (запущена внутри Б).
Все корутины запускаются и начинают свою работу. 
Корутина В выполняет delay(1000), корутина Г выполняет delay(500) и тоже приостанавливается.
Ошибка в родителе: Код в родительской корутине доходит до throw Exception(...). SupervisorJob: SupervisorJob
видит ошибку в своем ребенке и изолрует отказ. Он делает именно то, для чего создан: отменяет только того
ребенка, в котором произошла ошибка, но не трогает других.
Он отменяет "проблемного" ребенка, но он не отменяет его детей (наши корутины В и Г). Они продолжают жить.
Распространение ошибки (The Catch): Ошибка Exception начинает всплывать вверх по иерархии, ищя обработчик
try-catch. Она покидает SupervisorJob и идет к внешнему launch. Там обработчика нет. Исключение достигает
вершины иерархии (CoroutineScope). Если у него нет обработчика, оно попадает в CoroutineExceptionHandler
(если он установлен глобально) или просто приводит к падению приложения с выводом ошибки в консоль.
Финал: Процесс падения (crashing) приложения прерывает все потоки, включая те, где наши корутины могли бы
продолжить работу. Кроме того, даже если бы приложение не упало, корутины В и Г остались бы в подвешенном
состоянии, так как их родительский scope был отменен из-за ошибки, и они никогда бы не получили результат.
'''



