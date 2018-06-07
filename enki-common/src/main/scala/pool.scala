package Enki

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

import scala.concurrent.{Future, ExecutionContext}
import Enki.FutureOps._

import scala.collection.JavaConverters._
//import java.util.concurrent.atomic.{AtomicLong} 

case class Pool[T](
  list: ConcurrentLinkedQueue[T],
  create: Unit => Future[T], 
  close: T => Unit,
  max: Int,
  count: AtomicInteger
)

object Pool {

  def make[T](size: Int, make: Unit => Future[T], close: T => Unit)(implicit ec: ExecutionContext): Pool[T] = {

    val list = new ConcurrentLinkedQueue[T]()

    Pool(list, make, close, size, new AtomicInteger(0))
  }


  def createItem[T](pool: Pool[T])(implicit ec: ExecutionContext) = {
    pool.count.incrementAndGet() 
    pool.create()
  }



  def acquire[T](pool: Pool[T])(implicit ec: ExecutionContext) = {

    if (pool.list.isEmpty && (pool.count.get() < pool.max)) { createItem(pool) }
    else { Future( pool.list.poll() ) }

  }


  def release[T](pool: Pool[T], item: T)(implicit ec: ExecutionContext) = {
    pool.list.offer(item)
  }



  def use[T, U](pool: Pool[T], op: T => Future[U])(implicit ec: ExecutionContext) = {

    acquire(pool) flatMap {item =>
      op(item) ensure release(pool, item)
    }
  }


  def destroy[T](pool: Pool[T])(implicit ec: ExecutionContext) = {
    val l =  pool.list.toArray()

    l.toList.foreach { i =>
      val item = i.asInstanceOf[T]
      pool.close(item)
    }
  }

 
}
