# bills-exerc
A programming exercise about creating a bill generation service

A bill and its bill cycle of in the core of the credit card experience. Once a bill is opened
on open date, any purchase, credit, fee or payment posted to the customer’s account
will be added or subtracted from his/her balance. This lasts until the day before the close date,
when the customer is billed for all settled but unpaid charges made during the billing cycle.
Any activity on the account after the billing cycle ends will appear on the next billing
statement. The close date of a bill cycle triggers the open date of the next bill cycle.

A newly opened credit card account will start the first day of the first billing cycle
with a zero balance.

In this exercise a bill is modeled as a map of the following key value pairs
- status: any of the following keywords (future, open, closed, late or paid)
- due date: the last day until which the customer can pay the bill without
incurring in any penalties or interests
- effective date: if the due date falls in a holiday or weekend the customer has until
the nxt business day to paid the bill. This updated due date is called effective due date
- close date: the day before the close date is the last day on which any setlled but unpaid
purchase is included in the bill statement of that billing cycle
- open date: the first date of the billing cucle
- current date: the date on which the bill is generated
- line items: the list of all the sedttled purchases, credits, feed or paymentd posted to the
customer’s account during that billing cycle
- total: the net value of all the line items together
- amount paid: how much the customer paid of the bill total
- minimum payment: what is the minimium the customer must pay until the effective due date
so that it does not become late

In order to generate a bill the service must receive
- credit card account information containing
   - fx spread: how much spread this customer is charged on any transaction in a foreginer account
   - grace period: # of days between the close date and the due date
   - late interest APR: interest rate that is charge when the customer is late
   - late fee logic: a function on how to calculate the late fee for this customer
   - minimum pymt logic: a function on how to calculate the minimum payment
   - revolving interest APR: interest rate that is charge when the customer is revolving
   - contracted due day of the month: which day of the month the customer has chosen as his due day
   - time to recognize payment: payment methods used by customers to pay their bill can take days
   to be recognized by Nubank. So Nubank will only set the customer as late, after this amount of
   days have passed the effective due date. Interest will though be accrued considering the effective
   due date.
   - movements: all the sedtled purchases, credits, feed or paymentd posted to the customer’s account
- previous bill

NOTE: Many of the infos contained in the credit card account could set at a company level instead if a
account level, but in the future it could be powerful to be able to set different rates, or even
different logics of minimum payment and interest depending on the customer risk profile or credit
card spend (we want to offer 0 fx-spread for customer that travel a lot and spend more then 10K USD).

The service should have configured in itself
- list of holidays
- iva rate